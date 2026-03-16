package com.ddhva.ielts.dto.flashcard.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardRequest {
    @NotBlank(message = "Title is mandatory")
    private String title;
    private String description;
    @NotBlank(message = "Library Id is mandatory")
    private String libraryId;
    private String status;
}
