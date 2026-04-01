package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.exam.req.ExamRequest;
import com.ddhva.ielts.dto.exam.res.ExamResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.section.res.SectionResponse;
import com.ddhva.ielts.service.ExamService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exam Controller", description = "Exam Controller API")
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

    @GetMapping("/{examId}/sections")
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> getById(@PathVariable String id){
        ExamResponse response = examService.getExamById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> update(@PathVariable String id, @RequestBody ExamRequest request){
        ExamResponse res = examService.updateExam(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Update Successfully",
                        res
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id){
        examService.deleteExam(id);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Delete Successfully"
                )
        );
    }
}
