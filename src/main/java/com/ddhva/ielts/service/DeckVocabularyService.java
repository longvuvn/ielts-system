package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyUpdateRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.ReviewCount;
import com.ddhva.ielts.dto.deckvocabulary.req.ReviewRequest;
import com.ddhva.ielts.dto.deckvocabulary.res.AnswerDefinition;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.pagination.Pagination;

import java.util.List;

public interface DeckVocabularyService {
    Pagination<DeckVocabularyResponse> getAllDeckVocabularyByFlashcardId(String flashcardId, int page, int size);
    void createDeckVocabulary(DeckVocabularyRequest request);
    DeckVocabularyResponse getDeckVocabularyById(String deckVocabularyId);
    DeckVocabularyResponse updateDeckVocabulary(String deckVocabularyId, DeckVocabularyUpdateRequest request);
    void deleteDeckVocabulary(String deckVocabularyId);
    DeckVocabularyResponse countDeckVocabularyByFlashcardId(String deckVocabularyId);
    List<AnswerDefinition> userDefinition(String deckVocabularyId);
    void review(String id, ReviewRequest request);
}
