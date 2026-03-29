package com.ddhva.ielts.repositories;

import com.ddhva.ielts.dto.passage.PassageResponse;
import com.ddhva.ielts.model.Passage;
import com.ddhva.ielts.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PassageRepository extends JpaRepository<Passage, UUID> {

    @Query("""
    SELECT COUNT(p)
    FROM Passage p
    WHERE p.section.id = :sectionId
    """)
    long countBySection_Id(@Param("sectionId") UUID sectionId);

    @Query("""
    SELECT p
    FROM Passage p
    WHERE p.section.id = :sectionId
    AND p.passage_number = :passageNumber
    """)
    Optional<Passage> findBySectionIdAndPassageNumber(
            @Param("sectionId") UUID sectionId,
            @Param("passageNumber") Integer passageNumber);
}
