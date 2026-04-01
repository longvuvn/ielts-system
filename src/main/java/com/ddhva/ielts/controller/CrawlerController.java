package com.ddhva.ielts.controller;

import com.ddhva.ielts.service.ExamCrawlerService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
@Tag(name = "Crawler Controller", description = "Crawler Controller API")
public class CrawlerController {

    private final ExamCrawlerService examCrawlerService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Void>> run(@RequestParam(required = false) Integer limit) {
        try {
            examCrawlerService.crawlAndSave(limit);
            return ResponseEntity.ok(
                    new ApiResponse<>(HttpStatus.OK.value(), "Crawl completed successfully"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Crawl failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/answer-key")
    public ResponseEntity<ApiResponse<Void>> crawlAnswerKey(@RequestParam String resultsUrl) {
        try {
            examCrawlerService.crawlAndUpdateAnswerKey(resultsUrl);
            return ResponseEntity.ok(
                    new ApiResponse<>(HttpStatus.OK.value(), "Answer key updated successfully"));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(),
                            "Failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/answer-keys")
    public ResponseEntity<ApiResponse<Void>> crawlAnswerKeys(
            @RequestParam(required = false) Integer limit) {
        try {
            examCrawlerService.crawlAndUpdateAnswerKeysForExams(limit);
            return ResponseEntity.ok(
                    new ApiResponse<>(HttpStatus.OK.value(), "Answer keys updated. Check logs for details."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Failed: " + ex.getMessage()));
        }
    }
}