package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.exam.res.CrawledExamDto;
import com.ddhva.ielts.enums.*;
import com.ddhva.ielts.model.*;
import com.ddhva.ielts.repositories.*;
import com.ddhva.ielts.service.ExamPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExamPersistenceServiceImpl implements ExamPersistenceService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Override
    @Transactional
    public void save(CrawledExamDto dto) {
        try {
            if (examRepository.existsByTitle(dto.getTitle())) {
                log.info("[PERSISTENCE] Exam '{}' already exists, skipping.", dto.getTitle());
                return;
            }
            Exam exam = buildExam(dto.getTitle());
            exam = examRepository.save(exam);

            if (dto.getSections() == null) return;

            for (CrawledExamDto.CrawledSectionDto sDto : dto.getSections()) {
                if (sDto.getQuestions() == null || sDto.getQuestions().isEmpty()) continue;

                Section section = buildSection(sDto, exam);
                section = sectionRepository.save(section);

                for (CrawledExamDto.CrawledQuestionDto qDto : sDto.getQuestions()) {
                    if (qDto.getContent() == null || qDto.getContent().isBlank()) continue;

                    Question question = buildQuestion(qDto, section);
                    question = questionRepository.save(question);

                    if (qDto.getAnswers() == null) continue;
                    List<Answer> answers = buildAnswers(qDto.getAnswers(), question);
                    if (!answers.isEmpty()) answerRepository.saveAll(answers);
                }

                log.info("[PERSISTENCE] Section '{}' saved — {} questions",
                        section.getTitle(), sDto.getQuestions().size());
            }

            log.info("[PERSISTENCE] Exam '{}' saved", exam.getTitle());

        } catch (Exception e) {
            log.error("[PERSISTENCE] Error saving '{}': {}", dto.getTitle(), e.getMessage());
        }
    }

    private Exam buildExam(String title) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setStatus(ExamStatus.ACTIVE);
        exam.setMax_score(BigDecimal.valueOf(9.0));
        exam.setDuration(Instant.now().plus(170, ChronoUnit.MINUTES));
        return exam;
    }

    private Section buildSection(CrawledExamDto.CrawledSectionDto sDto, Exam exam) {
        Section section = new Section();
        section.setTitle(sDto.getTitle());
        section.setSection_number(sDto.getSectionNumber());
        section.setTime_limit(resolveTimeLimit(sDto.getSkillType()));
        section.setExam(exam);
        return section;
    }

    private Question buildQuestion(CrawledExamDto.CrawledQuestionDto qDto, Section section) {
        Question question = new Question();
        question.setContent(qDto.getContent());
        question.setType(resolveQuestionType(qDto.getQuestionType()));
        question.setLevel(LevelType.Medium);
        question.setScore(BigDecimal.ONE);
        question.setStatus(QuestionStatus.ACTIVE);
        question.setSection(section);
        return question;
    }

    private List<Answer> buildAnswers(List<CrawledExamDto.CrawledAnswerDto> aDtos, Question question) {
        List<Answer> answers = new ArrayList<>();
        for (CrawledExamDto.CrawledAnswerDto aDto : aDtos) {
            if (aDto.getContent() == null) continue;
            Answer answer = new Answer();
            answer.setContent(aDto.getContent());
            answer.setIs_correct(Boolean.TRUE.equals(aDto.getIsCorrect()));
            answer.setStatus(AnswerStatus.ACTIVE);
            answer.setQuestion(question);
            answers.add(answer);
        }
        return answers;
    }

    private Instant resolveTimeLimit(String skillType) {
        return switch (skillType.toUpperCase()) {
            case "LISTENING" -> Instant.now().plus(30, ChronoUnit.MINUTES);
            case "READING"   -> Instant.now().plus(60, ChronoUnit.MINUTES);
            case "WRITING"   -> Instant.now().plus(60, ChronoUnit.MINUTES);
            case "SPEAKING"  -> Instant.now().plus(15, ChronoUnit.MINUTES);
            default          -> Instant.now().plus(30, ChronoUnit.MINUTES);
        };
    }

    private QuestionType resolveQuestionType(String type) {
        if (type == null) return QuestionType.MULTIPLE_CHOICE;
        return switch (type.toUpperCase()) {
            case "FILL_IN_BLANK" -> QuestionType.FILL_IN_BLANK;
            case "TRUE_FALSE"    -> QuestionType.TRUE_FALSE;
            case "WRITING"       -> QuestionType.WRITING;
            case "SPEAKING"      -> QuestionType.SPEAKING;
            default              -> QuestionType.MULTIPLE_CHOICE;
        };
    }
}