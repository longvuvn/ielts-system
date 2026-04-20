package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.question.req.PracticeSubmitRequest;
import com.ddhva.ielts.dto.question.res.PracticeTestResponse;



public interface PracticeTestService {
    PracticeTestResponse generateFromWrongAnswers(String learnerId, String skill, String type, int page, int size);
    int submitPracticeAnswers(String learnerId, PracticeSubmitRequest request);
}
