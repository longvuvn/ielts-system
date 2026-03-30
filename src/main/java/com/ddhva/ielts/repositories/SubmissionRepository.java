package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Submissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submissions, UUID> {

    @Query("""
        SELECT s FROM Submissions s
        WHERE s.learner.id = :learnerId
        ORDER BY s.completedAt DESC
    """)
    List<Submissions> findByLearnerId(@Param("learnerId") UUID learnerId);

    @Query("""
        SELECT s FROM Submissions s
        WHERE s.learner.id = :learnerId
        ORDER BY s.completedAt DESC
    """)
    Page<Submissions> findByLearnerIdPageable(@Param("learnerId") UUID learnerId, Pageable pageable);
}