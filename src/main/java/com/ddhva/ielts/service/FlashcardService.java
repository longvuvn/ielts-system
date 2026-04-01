package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.flashcard.req.FlashcardRequest;
import com.ddhva.ielts.dto.flashcard.res.FlashcardResponse;
import com.ddhva.ielts.dto.pagination.Pagination;


public interface FlashcardService {
    Pagination<FlashcardResponse> getAllFlashcardsByLibraryId(String libraryId, int page, int size);
    Pagination<FlashcardResponse> searchFlashcard(String title, int page, int size);
    FlashcardResponse getFlashcardById(String flashcardId);
    FlashcardResponse createFlashcard(FlashcardRequest request);
    FlashcardResponse updateFlashcard(String flashcardId, FlashcardRequest request);
    void deleteFlashcard(String flashcardId);
}
