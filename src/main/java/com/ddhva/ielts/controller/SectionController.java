package com.ddhva.ielts.controller;


import com.ddhva.ielts.dto.passage.PassageResponse;
import com.ddhva.ielts.service.SectionService;
import com.ddhva.ielts.service.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/section")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;

    @GetMapping("/{sectionId}/content")
    public ResponseEntity<ApiResponse<List<PassageResponse>>> getQuestionBySectionId(@PathVariable String sectionId){
        List<PassageResponse> response = sectionService.getPassageBySectionId(sectionId);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get Successfully",
                        response
                )
        );
    }
}
