package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.submissions.req.SubmissionRequest;
import com.ddhva.ielts.dto.submissions.res.SubmissionResponse;
import com.ddhva.ielts.service.SubmissionService;
import com.ddhva.ielts.service.exception.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission Controller", description = "Submission Controller API")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubmissionResponse>> submit(@RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.createSubmission(request);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Submission created successfully",
                        response
                )
        );
    }
}
