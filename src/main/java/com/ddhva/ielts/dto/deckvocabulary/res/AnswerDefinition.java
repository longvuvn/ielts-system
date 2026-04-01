package com.ddhva.ielts.dto.deckvocabulary.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnswerDefinition {
    private String vocabularyId;
    private String definition;
    private String isCorrect;
}
