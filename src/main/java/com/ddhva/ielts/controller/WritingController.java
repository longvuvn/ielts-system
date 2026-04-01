package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.writing.req.WritingRequest;
import com.ddhva.ielts.dto.writing.res.WritingFeedbackResponse;
import com.ddhva.ielts.service.WritingGradingService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/writing")
@RequiredArgsConstructor
@Tag(name = "Writing Controller", description = "Writing Controller API")
public class WritingController {

    private final WritingGradingService writingGradingService;

    @PostMapping("/grade")
    public ResponseEntity<ApiResponse<WritingFeedbackResponse>> grade(@RequestBody WritingRequest req) {
        WritingFeedbackResponse response = writingGradingService.grade(req);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Writing Grade Successfully",
                        response
                )
        );
    }
}