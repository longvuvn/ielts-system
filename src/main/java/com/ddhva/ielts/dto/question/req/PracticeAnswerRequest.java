package com.ddhva.ielts.dto.question.req;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PracticeAnswerRequest {
    private String questionId;
    private String answerText;   // FILL_IN_BLANK
    private String answerId;     // MULTIPLE_CHOICE
}
