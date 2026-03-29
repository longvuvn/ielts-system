package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.service.LearnerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;

    // 🔥 map Entity → DTO
    private LearnerResponse mapToResponse(Learner learner) {
        return LearnerResponse.builder()
                .id(learner.getId().toString())
                .fullName(learner.getFullName())
                .email(learner.getEmail())
                .role(learner.getRole().getName())
                .build();
    }

    @Override
    public List<LearnerResponse> getAll() {
        return learnerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    public List<LearnerResponse> getAllDTO() {
        return learnerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}