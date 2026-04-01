package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.writing.req.WritingRequest;
import com.ddhva.ielts.dto.writing.res.WritingFeedbackResponse;

import java.util.UUID;

public interface WritingGradingService {
    WritingFeedbackResponse grade(WritingRequest writingRequest);
}