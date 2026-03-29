package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.service.LearnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learners")
@RequiredArgsConstructor
public class LearnerController {

    private final LearnerService learnerService;

    @GetMapping
    public List<LearnerResponse> getAll() {
        return learnerService.getAll();
    }

    @GetMapping("/dto")
    public List<LearnerResponse> getAllDTO() {
        return learnerService.getAllDTO();
    }
}