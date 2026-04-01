package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Library;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, UUID> {

    @Query("""
    SELECT l
    FROM Library l
    WHERE l.learner.id = :learnerId AND l.status = "ACTIVE"
""")
    Optional<Page<Library>> findByLearner_Id(UUID learnerId, Pageable pageable);

    @Query("""
    SELECT l
    FROM Library l
    WHERE l.is_Public = true
      AND LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    Optional<Page<Library>> searchLibrary(@Param("name") String name, Pageable pageable);
}
