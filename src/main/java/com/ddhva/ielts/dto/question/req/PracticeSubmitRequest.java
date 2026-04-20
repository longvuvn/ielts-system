package com.ddhva.ielts.dto.question.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeSubmitRequest {
    private List<PracticeAnswerRequest> answers;
}
