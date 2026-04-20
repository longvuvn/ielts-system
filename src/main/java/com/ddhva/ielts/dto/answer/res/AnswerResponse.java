package com.ddhva.ielts.dto.answer.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerResponse {
    private String id;
    private String content;
    private String status;
    private Boolean isCorrect;
}
