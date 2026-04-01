package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    // Đếm câu hỏi theo exam (qua passage -> section -> exam)
    @Query("""
        SELECT COUNT(q)
        FROM Question q
        WHERE q.passage.section.exam.id = :examId
    """)
    long countByPassage_Section_Exam_Id(@Param("examId") UUID examId);

    // Đếm câu hỏi theo section
    @Query("""
    SELECT COUNT(q)
    FROM Question q
    WHERE q.passage.section.id = :sectionId
    """)
    long countByPassage_Section_Id(@Param("sectionId") UUID sectionId);

    // Lấy content theo section
    @Query("""
    SELECT q.content
    FROM Question q
    WHERE q.passage.section.id = :sectionId
    """)
    List<String> findContentsByPassage_Section_Id(@Param("sectionId") UUID sectionId);

    // Lấy danh sách câu hỏi theo section sắp xếp theo createdAt
    @Query("""
    SELECT q
    FROM Question q
    WHERE q.passage.section.id = :sectionId
    ORDER BY q.createdAt ASC
    """)
    List<Question> findByPassage_Section_IdOrderByCreatedAtAsc(@Param("sectionId") UUID sectionId);

    // Tìm câu hỏi theo section và content
    @Query("""
    SELECT q
    FROM Question q
    WHERE q.passage.section.id = :sectionId
    AND q.content = :content
    """)
    Optional<Question> findByPassage_Section_IdAndContent(
            @Param("sectionId") UUID sectionId,
            @Param("content") String content);

    // Tìm câu hỏi theo passage
    List<Question> findByPassage_Id(UUID passageId);

    @Query("""
    SELECT p
    FROM Passage p
    JOIN Question q ON q.passage.id = p.id
    WHERE q.passage.id = :passageId
    """)
    List<Question> findByPassage_IdWithPassage(@Param("passageId") UUID passageId);

    @Modifying
    @Query("""
    DELETE
    FROM Question q
    WHERE q.passage.id IN (
        SELECT p.id FROM Passage p
        JOIN p.section s
        WHERE s.exam.id = :examId
    )
    """)
    void deleteQuestionsByExamId(@Param("examId") UUID examId);
}
