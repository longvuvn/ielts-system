package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
    boolean existsByTitle(String title);
}