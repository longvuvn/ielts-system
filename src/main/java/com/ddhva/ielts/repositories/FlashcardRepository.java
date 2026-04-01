package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FlashcardRepository extends JpaRepository <Flashcard, UUID>{
    @Query("""
    SELECT f
    FROM Flashcard f
    WHERE f.library.id = :libraryId AND f.status = "ACTIVE"
""")
    Optional<Page<Flashcard>> findByLibrary_Id(UUID libraryId, Pageable pageable);

    @Query("""
    SELECT f
    FROM Flashcard f
    WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :title, '%'))
""")
    Optional<Page<Flashcard>> searchTitle(@Param("title") String title, Pageable pageable);
}
