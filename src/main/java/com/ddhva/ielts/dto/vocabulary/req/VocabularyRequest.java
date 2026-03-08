package com.ddhva.ielts.dto.vocabulary.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyRequest {
    @NonNull
    private String topicId;

    @NotBlank(message = "Word is mandatory")
    private String word;

    @NotBlank(message = "IPA is mandatory")
    private String ipa;

    @NotBlank(message = "Example is mandatory")
    private String example;

    @NotBlank(message = "Audio URL is mandatory")
    private String audio_url;

    @NotBlank(message = "Definition is mandatory")
    private String definition;

    @NotBlank(message = "Part of speech is mandatory")
    private String part_of_speech;
    private String updatedAt;
}