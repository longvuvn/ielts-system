package com.ddhva.ielts.repositories;

import com.ddhva.ielts.dto.passage.PassageResponse;
import com.ddhva.ielts.model.Passage;
import com.ddhva.ielts.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepository extends JpaRepository <Section, UUID>{
    Optional<Section> findByExam_IdAndTitle(UUID examId, String title);

    @Query("""
        SELECT p
        FROM Passage p
        WHERE p.section.id = :sectionId
    """)
    Optional<List<Passage>> findByIdWithPassage(UUID sectionId);
}
