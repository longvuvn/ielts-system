package com.ddhva.ielts.controller;

import com.ddhva.ielts.service.ExamCrawlerService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final ExamCrawlerService examCrawlerService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Void>> run() {
        examCrawlerService.crawlAndSave();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Crawl triggered successfully"
                )
        );
    }
}
