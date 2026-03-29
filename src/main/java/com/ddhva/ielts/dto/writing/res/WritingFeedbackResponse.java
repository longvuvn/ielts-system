package com.ddhva.ielts.dto.writing.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WritingFeedbackResponse {
    private BigDecimal band;
    private String taskAchievement;
    private String coherenceCohesion;
    private String lexicalResource;
    private String grammaticalRange;
    private String overallFeedback;
    private String correctedEssay;
}