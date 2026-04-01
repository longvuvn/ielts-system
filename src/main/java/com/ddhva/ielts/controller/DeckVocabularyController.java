package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyUpdateRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.ReviewRequest;
import com.ddhva.ielts.dto.deckvocabulary.res.AnswerDefinition;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.DeckVocabularyService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deck-vocabulary")
@RequiredArgsConstructor
@Tag(name = "Deck Vocabulary Controller", description = "Deck Vocabulary Controller API")
public class DeckVocabularyController {

    private final DeckVocabularyService deckVocabularyService;


    @GetMapping("/flashcard/{flashcardId}")
    public ResponseEntity<ApiResponse<Pagination<DeckVocabularyResponse>>> getAllByFlashcardId(
            @PathVariable String flashcardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pagination<DeckVocabularyResponse> response =
                deckVocabularyService.getAllDeckVocabularyByFlashcardId(flashcardId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get All Successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckVocabularyResponse>> getById(@PathVariable String id){
        DeckVocabularyResponse response = deckVocabularyService.getDeckVocabularyById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@RequestBody DeckVocabularyRequest request){
        deckVocabularyService.createDeckVocabulary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Create Successfully"
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckVocabularyResponse>> update(@PathVariable String id, @RequestBody DeckVocabularyUpdateRequest request){
        DeckVocabularyResponse response = deckVocabularyService.updateDeckVocabulary(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Update Successfully",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id){
        deckVocabularyService.deleteDeckVocabulary(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete Successfully"
                )
        );
    }

    @PatchMapping("/{id}/count")
    public ResponseEntity<ApiResponse<DeckVocabularyResponse>> count( @PathVariable String id){
        DeckVocabularyResponse response = deckVocabularyService.countDeckVocabularyByFlashcardId(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Count Successfully",
                        response
                )
        );
    }

    @GetMapping("/study/quiz/{deckVocabularyId}")
    public ResponseEntity<ApiResponse<List<AnswerDefinition>>> getAnswerWrong(@PathVariable String deckVocabularyId){
        List<AnswerDefinition> res = deckVocabularyService.userDefinition(deckVocabularyId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        res
                )
        );
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<Void>> review (@PathVariable String id, @RequestBody ReviewRequest request){
        deckVocabularyService.review(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Review Successfully"
                )
        );
    }
}