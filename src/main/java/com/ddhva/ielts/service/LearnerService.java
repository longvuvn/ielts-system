package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.learner.req.LearnerUpdateRequest;
import com.ddhva.ielts.dto.learner.res.LearnerHistoryResponse;
import com.ddhva.ielts.dto.learner.res.LearnerResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LearnerService {
    List<LearnerResponse> getAll();
    List<LearnerResponse> getAllDTO();
    LearnerResponse getById(String id);
    LearnerResponse updateProfile(String id, LearnerUpdateRequest request);
    LearnerResponse updateAvatar(String id, MultipartFile file);
    LearnerHistoryResponse getHistory(String id, int page, int size);
}