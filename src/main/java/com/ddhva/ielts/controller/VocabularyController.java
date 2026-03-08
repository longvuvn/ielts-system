package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.service.exception.ApiResponse;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import com.ddhva.ielts.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/vocabularies")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping("/topic/{topicId}")
    public ResponseEntity<ApiResponse<Pagination<VocabularyResponse>>> getAllByTopicId(
            @PathVariable String topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pagination<VocabularyResponse> vocabularies = vocabularyService.getVocabularyByTopicId(topicId, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get All By TopicId Successfully",
                        vocabularies
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Pagination<VocabularyResponse>>> search(
            @RequestParam("word") String word,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pagination<VocabularyResponse> vocabularies = vocabularyService.searchVocabulary(word, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Search Successfully",
                        vocabularies
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> getById(@PathVariable String id) {
        VocabularyResponse vocabulary = vocabularyService.getVocabularyById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        vocabulary
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> importExcel(@RequestParam("file") MultipartFile file, @RequestParam("topicId") String topicId) {
        vocabularyService.importExcel(file, topicId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Successfully imported"
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyResponse>> update(@Valid @PathVariable String id, @RequestBody VocabularyRequest request) {
        VocabularyResponse res = vocabularyService.updateVocabulary(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Update Successfully",
                        res
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        vocabularyService.deleteVocabulary(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete Successfully"
                )
        );
    }
}