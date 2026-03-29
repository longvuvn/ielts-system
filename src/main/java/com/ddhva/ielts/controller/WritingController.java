package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.writing.WritingFeedbackResponse;
import com.ddhva.ielts.service.WritingGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/writing")
@RequiredArgsConstructor
public class WritingController {

    private final WritingGradingService writingGradingService;

    @PostMapping("/grade")
    public WritingFeedbackResponse grade(@RequestBody Map<String, String> req) {
        return writingGradingService.grade(
                req.get("task"),
                req.get("essay")
        );
    }
}