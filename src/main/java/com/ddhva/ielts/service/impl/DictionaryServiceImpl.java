package com.ddhva.ielts.service.impl;


import com.ddhva.ielts.dto.dictionary.DictionaryApiResponse;
import com.ddhva.ielts.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final RestTemplate restTemplate;

    @Value("${dictionary.api.url}")
    private String url;


    @Override
    public DictionaryApiResponse lookupWord(String word) {
        String URL = url + word;
        DictionaryApiResponse[] dictionaryApiResponse = restTemplate.getForObject(URL, DictionaryApiResponse[].class);
        if(dictionaryApiResponse != null && dictionaryApiResponse.length > 0){
            return dictionaryApiResponse[0];
        }
        return null;
    }
}
