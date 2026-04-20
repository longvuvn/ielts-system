package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Question;
import com.ddhva.ielts.model.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface SubmissionAnswerRepository extends JpaRepository<SubmissionAnswer, UUID> {

    @Query("""
        SELECT DISTINCT sa.question
        FROM SubmissionAnswer sa
        WHERE sa.submissions.learner.id = :learnerId
          AND sa.isCorrect = false
          AND (:skill IS NULL OR sa.submissions.exam.skillType = :skill)
          AND (:type IS NULL OR sa.question.type = :type)
          AND sa.question.id NOT IN (
              SELECT sa2.question.id FROM SubmissionAnswer sa2
              WHERE sa2.submissions.learner.id = :learnerId
              AND sa2.isCorrect = true
          )
        ORDER BY sa.question.question_number ASC
    """)
    List<Question> findWrongQuestionsByLearnerId(
            @Param("learnerId") UUID learnerId,
            @Param("skill") com.ddhva.ielts.enums.SkillType skill,
            @Param("type") com.ddhva.ielts.enums.QuestionType type,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(DISTINCT sa.question.id)
        FROM SubmissionAnswer sa
        WHERE sa.submissions.learner.id = :learnerId
          AND sa.isCorrect = false
          AND (:skill IS NULL OR sa.submissions.exam.skillType = :skill)
          AND (:type IS NULL OR sa.question.type = :type)
          AND sa.question.id NOT IN (
              SELECT sa2.question.id FROM SubmissionAnswer sa2
              WHERE sa2.submissions.learner.id = :learnerId
              AND sa2.isCorrect = true
          )
    """)
    long countWrongQuestionsByLearnerId(
            @Param("learnerId") UUID learnerId,
            @Param("skill") com.ddhva.ielts.enums.SkillType skill,
            @Param("type") com.ddhva.ielts.enums.QuestionType type
    );

    @Query("""
        SELECT sa FROM SubmissionAnswer sa
        WHERE sa.submissions.learner.id = :learnerId
          AND sa.question.id = :questionId
          AND sa.isCorrect = false
        ORDER BY sa.submissions.completedAt DESC
    """)
    List<SubmissionAnswer> findWrongAnswerByLearnerAndQuestion(
            @Param("learnerId") UUID learnerId,
            @Param("questionId") UUID questionId
    );
}
