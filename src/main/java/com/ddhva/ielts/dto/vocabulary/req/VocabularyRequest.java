package com.ddhva.ielts.dto.vocabulary.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VocabularyRequest {
    private String topicId;
    @NotBlank(message = "Word is mandatory")
    private String word;
    private String ipa;
    private String example;
    private String audio_url;
    private String definition;
    private String part_of_speech;
    private String createdAt;
    private String updatedAt;
}