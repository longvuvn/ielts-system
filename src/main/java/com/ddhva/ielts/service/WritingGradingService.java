package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.writing.WritingFeedbackResponse;

public interface WritingGradingService {
    WritingFeedbackResponse grade(String taskQuestion, String essayAnswer);
}