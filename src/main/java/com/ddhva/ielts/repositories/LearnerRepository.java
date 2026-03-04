package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LearnerRepository extends JpaRepository<Learner, UUID> {
}
