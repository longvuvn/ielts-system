package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.exam.res.ExamResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.section.res.SectionResponse;
import com.ddhva.ielts.service.ExamService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<ApiResponse<Pagination<ExamResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pagination<ExamResponse> response = examService.getAllExams(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSectionByExamId(@PathVariable String examId){
        List<SectionResponse> response = examService.getSectionByExamId(examId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }
}
