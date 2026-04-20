package com.ddhva.ielts.dto.question.res;

import com.ddhva.ielts.dto.answer.res.AnswerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PracticeResponse {
    private String id;
    private String passageHtml;
    private String instruction;
    private String questionText;
    private Integer questionNumber;
    private String audioUrl;
    private String type;
    private String level;
    private BigDecimal score;
    private List<AnswerResponse> answers;
}
