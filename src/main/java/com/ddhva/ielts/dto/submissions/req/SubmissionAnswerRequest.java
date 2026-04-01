package com.ddhva.ielts.dto.submissions.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionAnswerRequest {
    private String questionId;
    private String answerText;
    private String answerQuestion;
}
