package com.ddhva.ielts.dto.writing.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WritingRequest {
    @NotBlank(message = "Question ID is required")
    private String questionId;

    @NotBlank(message = "Answer cannot be empty")
    private String answerText;
}
