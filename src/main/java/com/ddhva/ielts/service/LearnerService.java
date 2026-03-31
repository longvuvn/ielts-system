package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.learner.res.LearnerResponse;

import java.util.List;

public interface LearnerService {
    List<LearnerResponse> getAll();
}