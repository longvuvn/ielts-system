package com.ddhva.ielts.dto.deckvocabulary.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCount {
    private String vocabularyId;
    private String flashcardId;
    private String reviewCount;
}

