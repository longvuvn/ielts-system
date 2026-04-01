package com.ddhva.ielts.dto.answer.res;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswerResponse {
    private String id;
    private String content;
    private String status;
    private Boolean isCorrect;
}
