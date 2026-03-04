package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VocabularyRepository extends JpaRepository <Vocabulary, UUID>{
}
