package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import org.springframework.web.multipart.MultipartFile;


public interface VocabularyService {
    Pagination<VocabularyResponse> getVocabularyByTopicId(String topicId, int page, int size);
    Pagination<VocabularyResponse> searchVocabulary(String word, int page, int size);
    VocabularyResponse getVocabularyById(String id);
    VocabularyResponse updateVocabulary(String id, VocabularyRequest request);
    void importExcel(MultipartFile file, String topicId);
    void deleteVocabulary(String id);
}