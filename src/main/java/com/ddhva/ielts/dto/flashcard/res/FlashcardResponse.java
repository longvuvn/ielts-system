package com.ddhva.ielts.dto.flashcard.res;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlashcardResponse {
    private String id;
    private String title;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;
}
