package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.submissions.req.SubmissionAnswerRequest;
import com.ddhva.ielts.dto.submissions.req.SubmissionRequest;
import com.ddhva.ielts.dto.submissions.res.SubmissionResponse;
import com.ddhva.ielts.enums.SubmissionStatus;
import com.ddhva.ielts.model.*;
import com.ddhva.ielts.repositories.*;
import com.ddhva.ielts.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionAnswerRepository submissionAnswerRepository;
    private final SubmissionRepository submissionRepository;
    private final ModelMapper modelMapper;
    private final QuestionRepository questionRepository;
    private final LearnerRepository learnerRepository;
    private final ExamRepository examRepository;
    private final AnswerRepository answerRepository;

    @Override
    public SubmissionResponse createSubmission(SubmissionRequest submissionRequest) {
        UUID learnerId = UUID.fromString(submissionRequest.getLearnerId());
        UUID examId = UUID.fromString(submissionRequest.getExamId());

        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new RuntimeException("Learner with id " + learnerId + " not found"));
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam with id " + examId + " not found"));

        Submissions submissions = modelMapper.map(submissionRequest, Submissions.class);
        submissions.setLearner(learner);
        submissions.setExam(exam);
        submissions.setStatus(SubmissionStatus.ACTIVE);
        submissions.setStartTime(Instant.now());
        submissions.setEndTime(Instant.now());
        submissions.setCompletedAt(Instant.now());
        submissions = submissionRepository.save(submissions);
        List<SubmissionAnswer> submissionAnswers = new ArrayList<>();
        submissions.setScore(calculateSubmissionScore(submissionRequest, submissions, submissionAnswers));
        submissions = submissionRepository.save(submissions);
        return modelMapper.map(submissions, SubmissionResponse.class);
    }


    private BigDecimal calculateSubmissionScore(SubmissionRequest submissionRequest,
                                                Submissions submissions,
                                                List<SubmissionAnswer> submissionAnswers) {
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;
        int failedCount = 0;

        if (submissionRequest.getSubmissionAnswerRequests() == null) {
            submissions.setTotalQuestions(0);
            submissions.setCorrectQuestions(0);
            submissions.setFailedQuestions(0);
            submissions.setScore(BigDecimal.valueOf(0.0));
            return BigDecimal.ZERO;
        }

        for (SubmissionAnswerRequest request : submissionRequest.getSubmissionAnswerRequests()) {
            UUID questionId = UUID.fromString(request.getQuestionId());

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question Not Found"));

            List<Answer> answerCorrect = answerRepository.findCorrectAnswersByQuestionId(question.getId());
            boolean isCorrect = isAnswerCorrect(request, answerCorrect);

            SubmissionAnswer answer = new SubmissionAnswer();
            answer.setSubmissions(submissions);
            answer.setQuestion(question);
            answer.setAnswerText(request.getAnswerText());
            answer.setAnswerOption(request.getAnswerQuestion());
            answer.setIs_correct(isCorrect);
            answer.setScore(isCorrect
                    ? (question.getScore() != null ? question.getScore() : BigDecimal.ONE)
                    : BigDecimal.ZERO);

            submissionAnswers.add(submissionAnswerRepository.save(answer));

            if (isCorrect) {
                correctCount++;
                totalScore = totalScore.add(answer.getScore());
            } else {
                failedCount++;
            }
        }

        submissions.setTotalQuestions(submissionRequest.getSubmissionAnswerRequests().size());
        submissions.setCorrectQuestions(correctCount);
        submissions.setFailedQuestions(failedCount);
        submissions.setScore(totalScore);

        return totalScore;
    }

    private boolean isAnswerCorrect(SubmissionAnswerRequest submissionAnswerRequest, List<Answer> answerCorrect) {
        String answerText = normalizeText(submissionAnswerRequest.getAnswerText());
        String answerOption = normalizeText(submissionAnswerRequest.getAnswerQuestion());

        for (Answer answer : answerCorrect) {
            String correctContent = normalizeText(answer.getContent());
            if (answerOption.contains(correctContent) || answerText.contains(correctContent)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String text) {
        return text == null ? "" : text.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
