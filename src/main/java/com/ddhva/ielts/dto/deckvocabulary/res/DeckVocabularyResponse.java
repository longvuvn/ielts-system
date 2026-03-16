package com.ddhva.ielts.dto.deckvocabulary.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckVocabularyResponse {
    private String id;
    private String flashcardId;
    private String vocabularyId;
    private String word;
    private String ipa;
    private String definition;
    private String example;
    private String audioUrl;
    private String userDefinition;
    private Integer reviewCount;
    private String status;
    private String lastReviewed;
    private String createdAt;
    private String updatedAt;
}
