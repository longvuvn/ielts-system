package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.submissions.req.SubmissionAnswerRequest;
import com.ddhva.ielts.dto.submissions.req.SubmissionRequest;
import com.ddhva.ielts.dto.submissions.res.SubmissionAnswerResponse;
import com.ddhva.ielts.dto.submissions.res.SubmissionResponse;
import com.ddhva.ielts.dto.writing.WritingFeedbackResponse;
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

        Submissions submissions = modelMapper.map(submissionRequest, Submissions.class);

        submissions.setLearner(learner);
        submissions.setExam(exam);
        submissions.setStatus(SubmissionStatus.ACTIVE);
        submissions.setStartTime(Instant.now());
        submissions.setCompletedAt(Instant.now());

        submissions = submissionRepository.save(submissions);

        List<SubmissionAnswer> submissionAnswers = new ArrayList<>();

        BigDecimal totalScore = calculateScore(submissionRequest, submissions, submissionAnswers);

        submissions.setScore(totalScore);
        submissions = submissionRepository.save(submissions);

        // ✅ map response + inject writing feedback
        SubmissionResponse response = modelMapper.map(submissions, SubmissionResponse.class);

        if (response.getSubmissionAnswers() != null) {
            for (SubmissionAnswerResponse ans : response.getSubmissionAnswers()) {

                if (ans.getWritingFeedback() == null && ans.getAnswerOption() != null) {
                    ans.setWritingFeedback(parseFeedback(ans.getAnswerOption()));
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

            // ================= WRITING =================
            if (question.getType() == QuestionType.WRITING) {

                WritingFeedbackResponse feedback = writingGradingService.grade(
                        question.getContent(),
                        answerRequest.getAnswerText()
                );

                BigDecimal band = feedback.getBand() != null
                        ? feedback.getBand()
                        : BigDecimal.ZERO;

                // 🎯 Normalize IELTS band (0.5 step)
                band = band.multiply(BigDecimal.valueOf(2))
                        .setScale(0, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(2));

                submissionAnswer.setScore(band);
                submissionAnswer.setIs_correct(band.compareTo(BigDecimal.valueOf(5.0)) >= 0);

                // ✅ FIX: lưu JSON đúng field
                submissionAnswer.setWritingFeedback(serializeFeedback(feedback));

                totalScore = totalScore.add(band);

                if (submissionAnswer.getIs_correct()) correctCount++;
                else failedCount++;
            }

            // ================= OTHER =================
            else {
                List<Answer> correctAnswers = answerRepository.findCorrectAnswersByQuestionId(question.getId());

                boolean isCorrect = isAnswerCorrect(answerRequest, correctAnswers);

                submissionAnswer.setIs_correct(isCorrect);

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
            return feedback.getOverallFeedback();
        }
    }

    private WritingFeedbackResponse parseFeedback(String json) {
        try {
            return objectMapper.readValue(json, WritingFeedbackResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAnswerCorrect(SubmissionAnswerRequest req, List<Answer> correctAnswers) {

        String answerText   = normalizeText(req.getAnswerText());
        String answerOption = normalizeText(req.getAnswerQuestion());

        for (Answer answer : correctAnswers) {
            String correct = normalizeText(answer.getContent());

            if (answerOption.contains(correct) || answerText.contains(correct)) {
                return true;
            }
        }

        return false;
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}