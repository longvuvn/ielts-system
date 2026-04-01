package com.ddhva.ielts.repositories;

import com.ddhva.ielts.model.DeckVocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckVocabularyRepository extends JpaRepository <DeckVocabulary, UUID>{

    @Query("""
    SELECT d
    FROM DeckVocabulary d
    WHERE d.flashcard.id = :flashcardId
""")
    Optional<Page<DeckVocabulary>> findByFlashcard_Id(UUID flashcardId, Pageable pageable);

    @Query("""
    SELECT d
    FROM DeckVocabulary d
    WHERE d.id = :deckVocabularyId
    """)
    List<DeckVocabulary> findCorrectAnswer(UUID deckVocabularyId);

    @Query("""
    SELECT d FROM DeckVocabulary d
    WHERE d.flashcard.id = :flashcardId
    AND d.vocabulary.id <> :vocabularyId
""")
    List<DeckVocabulary> findWrongAnswers(UUID flashcardId, UUID vocabularyId);
}
