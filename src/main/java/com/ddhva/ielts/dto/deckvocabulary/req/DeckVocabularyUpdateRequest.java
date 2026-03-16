package com.ddhva.ielts.dto.deckvocabulary.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckVocabularyUpdateRequest {
    private String flashcardId;
    private String vocabularyId;
    private String userDefinition;
    private String status;
}
