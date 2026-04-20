package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.submissions.req.SubmissionAnswerRequest;
import com.ddhva.ielts.dto.submissions.req.SubmissionRequest;
import com.ddhva.ielts.dto.submissions.res.SubmissionAnswerResponse;
import com.ddhva.ielts.dto.submissions.res.SubmissionResponse;
import com.ddhva.ielts.dto.writing.req.WritingRequest;
import com.ddhva.ielts.dto.writing.res.WritingFeedbackResponse;
import com.ddhva.ielts.enums.QuestionType;
import com.ddhva.ielts.enums.SubmissionStatus;
import com.ddhva.ielts.model.*;
import com.ddhva.ielts.repositories.*;
import com.ddhva.ielts.service.SubmissionService;
import com.ddhva.ielts.service.WritingGradingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final SubmissionRepository submissionRepository;
    private final ModelMapper modelMapper;
    private final QuestionRepository questionRepository;
    private final LearnerRepository learnerRepository;
    private final ExamRepository examRepository;
    private final AnswerRepository answerRepository;
    private final WritingGradingService writingGradingService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SubmissionResponse createSubmission(SubmissionRequest submissionRequest) {

        UUID learnerId = UUID.fromString(submissionRequest.getLearnerId());
        UUID examId    = UUID.fromString(submissionRequest.getExamId());

        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner not found"));

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Submissions submissions = new Submissions();
        submissions.setLearner(learner);
        submissions.setExam(exam);
        submissions.setStatus(SubmissionStatus.ACTIVE);
        submissions.setStartTime(Instant.now());
        submissions.setCompletedAt(Instant.now());

        submissions = submissionRepository.save(submissions);

        List<SubmissionAnswer> submissionAnswers = new ArrayList<>();

        BigDecimal totalScore = calculateScore(submissionRequest, submissions, submissionAnswers);

        submissions.setScore(totalScore);
        submissions.setSubmissionAnswers(submissionAnswers);
        submissions = submissionRepository.save(submissions);

        SubmissionResponse response = modelMapper.map(submissions, SubmissionResponse.class);
        
        // Manual mapping for fields that might not match naming conventions
        response.setLearnerId(learnerId.toString());
        response.setExamId(examId.toString());
        response.setTotalQuestions(String.valueOf(submissions.getTotalQuestions()));
        response.setCorrectAnswer(String.valueOf(submissions.getCorrectQuestions()));
        response.setFailedAnswer(String.valueOf(submissions.getFailedQuestions()));
        response.setScore(submissions.getScore().toString());
        response.setCompleted_At(submissions.getCompletedAt().toString());
        response.setStarted_At(submissions.getStartTime().toString());

        if (response.getSubmissionAnswers() != null) {
            for (int i = 0; i < submissionAnswers.size(); i++) {
                SubmissionAnswer entity = submissionAnswers.get(i);
                SubmissionAnswerResponse resAns = response.getSubmissionAnswers().get(i);
                
                resAns.setIs_correct(String.valueOf(entity.getIsCorrect()));
                resAns.setQuestion_id(entity.getQuestion().getId().toString());
                
                if (entity.getQuestion().getType() == QuestionType.WRITING && entity.getWritingFeedback() != null) {
                    resAns.setWritingFeedback(parseFeedback(entity.getWritingFeedback()));
                }
            }
        }

        return response;
    }

    private BigDecimal calculateScore(SubmissionRequest request,
                                      Submissions submissions,
                                      List<SubmissionAnswer> submissionAnswers) {

        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        int failedCount  = 0;

        if (request.getSubmissionAnswerRequests() == null) {
            submissions.setTotalQuestions(0);
            submissions.setCorrectQuestions(0);
            submissions.setFailedQuestions(0);
            return BigDecimal.ZERO;
        }

        for (SubmissionAnswerRequest answerRequest : request.getSubmissionAnswerRequests()) {

            UUID questionId = UUID.fromString(answerRequest.getQuestionId());

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            SubmissionAnswer submissionAnswer = new SubmissionAnswer();

            submissionAnswer.setSubmissions(submissions);
            submissionAnswer.setQuestion(question);
            submissionAnswer.setAnswerText(answerRequest.getAnswerText());
            submissionAnswer.setAnswerOption(answerRequest.getAnswerQuestion());
            
            if (question.getType() == QuestionType.WRITING) {
                WritingRequest writingRequest = new WritingRequest();
                writingRequest.setQuestionId(questionId.toString());
                writingRequest.setAnswerText(answerRequest.getAnswerText());
                
                WritingFeedbackResponse feedback = writingGradingService.grade(writingRequest);
                BigDecimal band = feedback.getBand() != null
                        ? feedback.getBand()
                        : BigDecimal.ZERO;
                
                // Original logic: doubling the band for scoring
                BigDecimal doubledBand = band.multiply(BigDecimal.valueOf(2))
                        .setScale(0, RoundingMode.HALF_UP);
                
                submissionAnswer.setScore(doubledBand);
                submissionAnswer.setIsCorrect(doubledBand.compareTo(BigDecimal.valueOf(5.0)) >= 0);
                submissionAnswer.setWritingFeedback(serializeFeedback(feedback));

                totalScore = totalScore.add(doubledBand);

                if (submissionAnswer.getIsCorrect()) correctCount++;
                else failedCount++;
            }
            else {
                List<Answer> correctAnswers = answerRepository.findCorrectAnswersByQuestionId(question.getId());

                boolean isCorrect = isAnswerCorrect(answerRequest, correctAnswers);

                submissionAnswer.setIsCorrect(isCorrect);

                submissionAnswer.setScore(isCorrect
                        ? (question.getScore() != null ? question.getScore() : BigDecimal.ONE)
                        : BigDecimal.ZERO);

                if (isCorrect) {
                    correctCount++;
                    totalScore = totalScore.add(submissionAnswer.getScore());
                } else {
                    failedCount++;
                }
            }

            submissionAnswers.add(submissionAnswerRepository.save(submissionAnswer));
        }

        submissions.setTotalQuestions(request.getSubmissionAnswerRequests().size());
        submissions.setCorrectQuestions(correctCount);
        submissions.setFailedQuestions(failedCount);

        return totalScore;
    }

    private String serializeFeedback(WritingFeedbackResponse feedback) {
        try {
            return objectMapper.writeValueAsString(feedback);
        } catch (Exception e) {
            return "{\"overallFeedback\":\"" + feedback.getOverallFeedback() + "\"}";
        }
    }

    private WritingFeedbackResponse parseFeedback(String json) {
        try {
            return objectMapper.readValue(json, WritingFeedbackResponse.class);
        } catch (Exception e) {
            WritingFeedbackResponse fallback = new WritingFeedbackResponse();
            fallback.setOverallFeedback(json);
            return fallback;
        }
    }

    private boolean isAnswerCorrect(SubmissionAnswerRequest req, List<Answer> correctAnswers) {

        String answerText   = normalizeText(req.getAnswerText());
        String answerOption = normalizeText(req.getAnswerQuestion());

        if (answerText.isEmpty() && answerOption.isEmpty()) {
            return false;
        }

        for (Answer answer : correctAnswers) {
            // 1. Kiểm tra theo ID (nếu answerText hoặc answerOption là UUID và khớp với ID của đáp án đúng)
            String answerIdStr = answer.getId().toString();
            if (req.getAnswerText() != null && req.getAnswerText().equalsIgnoreCase(answerIdStr)) {
                return true;
            }
            if (req.getAnswerQuestion() != null && req.getAnswerQuestion().equalsIgnoreCase(answerIdStr)) {
                return true;
            }

            // 2. Kiểm tra theo Text
            String correct = normalizeText(answer.getContent());
            if (correct.isEmpty()) continue;

            // So khớp trực tiếp
            if (answerOption.equals(correct) || answerText.equals(correct)) {
                return true;
            }

            // Hỗ trợ trường hợp đáp án trong DB chứa nhiều phương án cách nhau bởi dấu /
            if (correct.contains("/")) {
                String[] parts = correct.split("/");
                for (String part : parts) {
                    String normalizedPart = normalizeText(part);
                    if (answerOption.equals(normalizedPart) || answerText.equals(normalizedPart)) {
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
}