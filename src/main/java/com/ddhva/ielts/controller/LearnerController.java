package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.learner.req.LearnerUpdateRequest;
import com.ddhva.ielts.dto.learner.res.LearnerHistoryResponse;
import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.service.LearnerService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
@RequiredArgsConstructor
@Tag(name = "Learner Controller", description = "Learner Controller API")
public class LearnerController {

    private final LearnerService learnerService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LearnerResponse>>> getAll() {
        List<LearnerResponse> response = learnerService.getAll();
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Get All Successfully", response)
        );
    }

    @GetMapping("/dto")
    public ResponseEntity<ApiResponse<List<LearnerResponse>>> getAllDTO() {
        List<LearnerResponse> response = learnerService.getAllDTO();
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Get All Successfully", response)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LearnerResponse>> getById(@PathVariable String id) {
        LearnerResponse response = learnerService.getById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Get Successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LearnerResponse>> updateProfile(
            @PathVariable String id,
            @RequestBody LearnerUpdateRequest request) {
        LearnerResponse response = learnerService.updateProfile(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Update Successfully", response)
        );
    }

    @PatchMapping(value = "/{id}/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LearnerResponse>> updateAvatar(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        LearnerResponse response = learnerService.updateAvatar(id, file);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Avatar Updated Successfully", response)
        );
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<LearnerHistoryResponse>> getHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LearnerHistoryResponse response = learnerService.getHistory(id, page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Get History Successfully", response)
        );
    }
}