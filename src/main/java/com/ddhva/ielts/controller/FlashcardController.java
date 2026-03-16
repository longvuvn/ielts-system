package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.flashcard.req.FlashcardRequest;
import com.ddhva.ielts.dto.flashcard.res.FlashcardResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.FlashcardService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/flashcard")
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    @GetMapping("/library/{libraryId}")
    public ResponseEntity<ApiResponse<Pagination<FlashcardResponse>>> getAllByLearnerId(
            @PathVariable String libraryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pagination<FlashcardResponse> response = flashcardService.getAllFlashcardsByLibraryId(libraryId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get All Successfully",
                        response
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Pagination<FlashcardResponse>>> search(
            @RequestParam("title") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pagination<FlashcardResponse> response = flashcardService.searchFlashcard(title, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Search Successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getById(@PathVariable String id){
        FlashcardResponse response = flashcardService.getFlashcardById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> create(@RequestBody FlashcardRequest request){
        FlashcardResponse response = flashcardService.createFlashcard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Created Flashcard Successfully",
                        response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> update(@RequestBody FlashcardRequest request, @PathVariable String id){
        FlashcardResponse response = flashcardService.updateFlashcard(id, request);
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
        flashcardService.deleteFlashcard(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete Successfully"
                )
        );
    }
}
