package com.ddhva.ielts.dto.question.res;


import com.ddhva.ielts.dto.answer.res.AnswerResponse;
import com.ddhva.ielts.enums.LevelType;
import com.ddhva.ielts.enums.QuestionStatus;
import com.ddhva.ielts.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {
    private UUID id;
    private String question_text;
    private String question_number;
    private String content;
    private QuestionType type;
    private QuestionStatus status;
    private BigDecimal score;
    private LevelType level;
    private String notes;
}
