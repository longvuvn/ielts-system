package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.service.LearnerService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
@RequiredArgsConstructor
public class LearnerController {

    private final LearnerService learnerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearnerResponse>>> getAll() {
        List<LearnerResponse> learners = learnerService.getAll();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get All Successfully",
                        learners
                )
        );
    }
}