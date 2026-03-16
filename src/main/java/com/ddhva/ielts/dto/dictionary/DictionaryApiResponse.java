package com.ddhva.ielts.dto.dictionary;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryApiResponse {
    private String word;
    private List<PhoneTic> phonetics;
    private List<Meaning> meanings;
    private String ipa;
    private String audioUrl;
    private String definition;
    private String example;
    private String partOfSpeech;

    public String getIpa() {
        if (phonetics != null && !phonetics.isEmpty()) {
            return phonetics.stream()
                    .map(PhoneTic::getText)
                    .filter(text -> text != null && !text.isEmpty())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String getAudioUrl() {
        if (phonetics != null) {
            return phonetics.stream()
                    .map(PhoneTic::getAudio)
                    .filter(audio -> audio != null && !audio.isEmpty())
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public String getDefinition() {
        if (meanings != null && !meanings.isEmpty()) {
            List<Definition> defs = meanings.getFirst().getDefinitions();
            if (defs != null && !defs.isEmpty()) {
                return defs.getFirst().getDefinition();
            }
        }
        return null;
    }

    public String getExample() {
        if (meanings != null && !meanings.isEmpty()) {
            List<Definition> defs = meanings.getFirst().getDefinitions();
            if (defs != null && !defs.isEmpty()) {
                return defs.getFirst().getExample();
            }
        }
        return null;
    }

    public String getPartOfSpeech() {
        if (meanings != null && !meanings.isEmpty()) {
            return meanings.getFirst().getPartOfSpeech();
        }
        return null;
    }
}