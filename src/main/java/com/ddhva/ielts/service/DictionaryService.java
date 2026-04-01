package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.dictionary.DictionaryApiResponse;

public interface DictionaryService {
    DictionaryApiResponse lookupWord(String word);
}
