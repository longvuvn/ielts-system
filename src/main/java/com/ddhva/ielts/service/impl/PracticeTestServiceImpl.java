package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.answer.res.AnswerResponse;
import com.ddhva.ielts.dto.question.req.PracticeAnswerRequest;
import com.ddhva.ielts.dto.question.req.PracticeSubmitRequest;
import com.ddhva.ielts.dto.question.res.PracticeGroupResponse;
import com.ddhva.ielts.dto.question.res.PracticeResponse;
import com.ddhva.ielts.dto.question.res.PracticeTestResponse;
import com.ddhva.ielts.enums.QuestionType;
import com.ddhva.ielts.enums.SkillType;
import com.ddhva.ielts.model.Question;
import com.ddhva.ielts.model.SubmissionAnswer;
import com.ddhva.ielts.repositories.AnswerRepository;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.repositories.SubmissionAnswerRepository;
import com.ddhva.ielts.service.PracticeTestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeTestServiceImpl implements PracticeTestService {

    private final SubmissionAnswerRepository submissionAnswerRepo;
    private final LearnerRepository learnerRepo;
    private final AnswerRepository answerRepository;

    @Override
    public PracticeTestResponse generateFromWrongAnswers(String learnerId, String skill, String type, int page, int size) {
        UUID learnerUUID = UUID.fromString(learnerId);
        log.info("[PRACTICE-TEST] Generating for learner: {}, skill: {}, type: {}, page: {}, size: {}", learnerId, skill, type, page, size);

        // Parse Enums
        SkillType skillEnum = null;
        if (skill != null && !skill.isEmpty()) {
            try { skillEnum = SkillType.valueOf(skill.toUpperCase()); } catch (Exception ignored) {}
        }
        QuestionType typeEnum = null;
        if (type != null && !type.isEmpty()) {
            try { typeEnum = QuestionType.valueOf(type.toUpperCase()); } catch (Exception ignored) {}
        }

        // Kiểm tra learner tồn tại
        learnerRepo.findById(learnerUUID)
                .orElseThrow(() -> new RuntimeException("Learner not found: " + learnerId));

        Pageable pageable = PageRequest.of(page, size);
        List<Question> wrongQuestions = submissionAnswerRepo
                .findWrongQuestionsByLearnerId(learnerUUID, skillEnum, typeEnum, pageable);

        long totalWrong = submissionAnswerRepo
                .countWrongQuestionsByLearnerId(learnerUUID, skillEnum, typeEnum);

        if (wrongQuestions.isEmpty()) {
            return PracticeTestResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .totalQuestions(0)
                    .totalWrongQuestions(totalWrong)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(size)
                    .groups(List.of())
                    .build();
        }

        // Group questions by Passage/Context
        // Use LinkedHashMap to preserve the order of questions from DB
        Map<String, PracticeGroupResponse> groupMap = new LinkedHashMap<>();

        for (Question q : wrongQuestions) {
            String contextId = "standalone";
            String passageHtml;
            String instruction;
            String audioUrl;

            if (q.getPassage() != null) {
                contextId = q.getPassage().getId().toString();
                passageHtml = q.getPassage().getContent_html();
                instruction = q.getPassage().getInstruction();
                if (q.getPassage().getSection() != null) {
                    audioUrl = q.getPassage().getSection().getAudio_url();
                } else {
                    audioUrl = null;
                }
            } else {
                audioUrl = null;
                instruction = null;
                passageHtml = null;
            }

            PracticeGroupResponse group = groupMap.computeIfAbsent(contextId, k -> PracticeGroupResponse.builder()
                    .passageId(k.equals("standalone") ? null : k)
                    .passageHtml(passageHtml)
                    .instruction(instruction)
                    .audioUrl(audioUrl)
                    .questions(new ArrayList<>())
                    .build());

            group.getQuestions().add(toQuestionResponse(q));
        }

        int totalPages = (int) Math.ceil((double) totalWrong / size);

        return PracticeTestResponse.builder()
                .id(UUID.randomUUID().toString())
                .totalQuestions(wrongQuestions.size())
                .totalWrongQuestions(totalWrong)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .groups(new ArrayList<>(groupMap.values()))
                .build();
    }

    @Override
    @Transactional
    public int submitPracticeAnswers(String learnerId, PracticeSubmitRequest request) {
        UUID learnerUUID = UUID.fromString(learnerId);
        int correctCount = 0;

        // Kiểm tra learner tồn tại
        learnerRepo.findById(learnerUUID)
                .orElseThrow(() -> new RuntimeException("Learner not found: " + learnerId));

        for (PracticeAnswerRequest answer : request.getAnswers()) {
            UUID questionId = UUID.fromString(answer.getQuestionId());

            List<SubmissionAnswer> wrongAnswers = submissionAnswerRepo
                    .findWrongAnswerByLearnerAndQuestion(learnerUUID, questionId);

            if (wrongAnswers.isEmpty()) continue;

            boolean isNowCorrect = checkAnswer(answer, wrongAnswers.get(0).getQuestion());

            if (isNowCorrect) {
                correctCount++;
                // Cập nhật tất cả các bản ghi sai cho câu hỏi này thành đúng (để loại khỏi danh sách ôn tập)
                for (SubmissionAnswer sa : wrongAnswers) {
                    sa.setIsCorrect(true);
                    submissionAnswerRepo.save(sa);
                }
                log.info("[PRACTICE] Learner {} answered question {} correctly", learnerId, questionId);
            }
        }
        return correctCount;
    }

    private boolean checkAnswer(PracticeAnswerRequest req, Question question) {
        List<com.ddhva.ielts.model.Answer> correctAnswers = answerRepository.findCorrectAnswersByQuestionId(question.getId());
        
        String answerText = normalizeText(req.getAnswerText());
        String answerId = req.getAnswerId();

        if (answerText.isEmpty() && (answerId == null || answerId.isEmpty())) {
            return false;
        }

        for (com.ddhva.ielts.model.Answer answer : correctAnswers) {
            // 1. Kiểm tra theo ID (nếu người dùng gửi answerId)
            if (answerId != null && !answerId.isEmpty()) {
                if (answer.getId().toString().equalsIgnoreCase(answerId)) {
                    return true;
                }
            }

            // 2. Kiểm tra theo Text (nếu người dùng gửi answerText)
            String correct = normalizeText(answer.getContent());
            if (correct.isEmpty()) continue;

            // So khớp tuyệt đối hoặc nếu đáp án trong DB có dạng "A/B" thì tách ra để so khớp
            if (answerText.equals(correct)) {
                return true;
            }
            
            // Hỗ trợ trường hợp đáp án trong DB chứa nhiều phương án cách nhau bởi dấu /
            if (correct.contains("/")) {
                String[] parts = correct.split("/");
                for (String part : parts) {
                    if (answerText.equals(normalizeText(part))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private PracticeResponse toQuestionResponse(Question q) {
        String passageHtml = null;
        String instruction = null;
        String audioUrl = null;
        if (q.getPassage() != null) {
            passageHtml = q.getPassage().getContent_html();
            instruction = q.getPassage().getInstruction();

            // Lấy audio từ Section
            if (q.getPassage().getSection() != null) {
                audioUrl = q.getPassage().getSection().getAudio_url();
            }
        }

        List<AnswerResponse> answers = q.getAnswer() == null ? List.of() :
                q.getAnswer().stream()
                .map(a -> AnswerResponse.builder()
                          .id(a.getId().toString())
                          .content(a.getContent())
                          .build())
                .toList();

        return PracticeResponse.builder()
                .id(q.getId().toString())
                .passageHtml(passageHtml)
                .instruction(instruction)
                .audioUrl(audioUrl)
                .questionText(q.getQuestion_text())
                .questionNumber(q.getQuestion_number())
                .type(q.getType() != null ? q.getType().name() : null)
                .level(q.getLevel() != null ? q.getLevel().name() : null)
                .score(q.getScore())
                .answers(answers)
                .build();
    }
}