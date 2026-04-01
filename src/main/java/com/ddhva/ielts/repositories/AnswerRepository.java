package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {

    @Query("""
        SELECT a.content
        FROM Answer a
        WHERE a.question.id = :questionId
    """)
    List<String> findContentsByQuestion_Id(@Param("questionId") UUID questionId);

    @Query("""
        SELECT a
        FROM Answer a
        WHERE a.question.passage.section.id = :sectionId
    """)
    List<Answer> findByQuestion_Passage_Section_Id(@Param("sectionId") UUID sectionId);

    @Query("""
        SELECT a
        FROM Answer a
        WHERE a.question.passage.section.exam.id = :examId
    """)
    List<Answer> findByQuestion_Passage_Section_Exam_Id(@Param("examId") UUID examId);

    @Query("""
        SELECT COUNT(a) FROM Answer a
        WHERE a.question.passage.section.id = :sectionId
        AND a.is_correct = :correct
    """)
    long countByQuestion_Passage_Section_IdAndIs_correctTrue(
            @Param("sectionId") UUID sectionId,
            @Param("correct") Boolean correct);

    @Query("""
        SELECT COUNT(DISTINCT a.question.id) FROM Answer a
        WHERE a.question.passage.section.id = :sectionId
        AND a.is_correct = :correct
    """)
    long countDistinctQuestionsByPassage_Section_IdAndIs_correctTrue(
            @Param("sectionId") UUID sectionId,
            @Param("correct") Boolean correct);

    List<Answer> findByQuestion_Id(UUID questionId);

    @Query("""
        SELECT a
        FROM Answer a
        WHERE a.question.id = :questionId
        AND a.is_correct = true
    """)
    List<Answer> findCorrectAnswersByQuestionId(@Param("questionId") UUID questionId);
}