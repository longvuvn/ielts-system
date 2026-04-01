package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Exam;
import com.ddhva.ielts.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    Optional<Exam> findByTitle(String title);

    @Query("""
    SELECT s
    FROM Section s
    WHERE s.exam.id = :examId
    ORDER BY
        s.section_number ASC
""")
    List<Section> findByExam_Id(UUID examId);

    @Modifying
    @Query("""
    DELETE
    FROM Exam e
    WHERE e.id = :examId
    """)
    void deleteExamById(@Param("examId") UUID examId);
}