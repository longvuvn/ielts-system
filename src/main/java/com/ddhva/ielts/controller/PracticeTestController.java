package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.question.req.PracticeSubmitRequest;
import com.ddhva.ielts.dto.question.res.PracticeTestResponse;
import com.ddhva.ielts.service.PracticeTestService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/practice")
@RequiredArgsConstructor
public class PracticeTestController {

    private final PracticeTestService practiceTestService;

    @GetMapping("/wrong-answers")
    public ResponseEntity<ApiResponse<PracticeTestResponse>> generatePracticeTest(
            @RequestParam String learnerId,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PracticeTestResponse response = practiceTestService
                .generateFromWrongAnswers(learnerId, skill, type, page, size);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Get Successfully", response)
        );
    }

    @PostMapping("/wrong-answers/submit")
    public ResponseEntity<ApiResponse<Integer>> submitPractice(
            @RequestParam String learnerId,
            @RequestBody PracticeSubmitRequest submitRequest) {

        int correctCount = practiceTestService.submitPracticeAnswers(learnerId, submitRequest);

        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Submitted Successfully", correctCount)
        );
    }
}