package com.ddhva.ielts.dto.passage;

import com.ddhva.ielts.dto.question.res.QuestionResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassageResponse {
    private UUID id;
    private String content_html;
    private Integer passage_number;
    private String instruction;
    private String audio_url;
    private String image_url;
    List<QuestionResponse> questions;
}
