package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Submissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubmissionRepository extends JpaRepository <Submissions, UUID>{
}
