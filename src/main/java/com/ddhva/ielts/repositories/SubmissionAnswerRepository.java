package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.SubmissionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubmissionAnswerRepository extends JpaRepository <SubmissionAnswer, UUID>{
}
