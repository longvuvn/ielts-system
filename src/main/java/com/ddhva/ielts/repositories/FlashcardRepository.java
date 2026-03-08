package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FlashcardRepository extends JpaRepository <Flashcard, UUID>{
}
