package com.ddhva.ielts.dto.deckvocabulary.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeckVocabularyRequest {
    private String vocabularyId;

    @NotBlank(message = "Word is mandatory")
    private String word;
    private String ipa;
    private String example;
    private String audio_url;
    private String definition;
    private String part_of_speech;
    @NotBlank(message = "Flashcard Id is mandatory")
    private String flashcardId;

    @NotBlank(message = "User definition is mandatory")
    private String userDefinition;
}
