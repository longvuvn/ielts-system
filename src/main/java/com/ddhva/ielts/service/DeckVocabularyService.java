package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyUpdateRequest;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.pagination.Pagination;

public interface DeckVocabularyService {
    Pagination<DeckVocabularyResponse> getAllDeckVocabularyByFlashcardId(String flashcardId, int page, int size);
    void createDeckVocabulary(DeckVocabularyRequest request);
    DeckVocabularyResponse getDeckVocabularyById(String deckVocabularyId);
    DeckVocabularyResponse updateDeckVocabulary(String deckVocabularyId, DeckVocabularyUpdateRequest request);
    void deleteDeckVocabulary(String deckVocabularyId);
}
