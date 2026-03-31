package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.service.LearnerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<LearnerResponse> getAll() {
        List<Learner> learners = learnerRepository.findAll();
        return learners.stream()
                .map(learner -> modelMapper.map(learner, LearnerResponse.class))
                .collect(Collectors.toList());
    }
}