package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerRepository extends JpaRepository<Answer, UUID> {
}
