package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LearnerRepository learnerRepository;

}
