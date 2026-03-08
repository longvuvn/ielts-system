package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import com.ddhva.ielts.enums.VocabularyStatus;
import com.ddhva.ielts.model.Topic;
import com.ddhva.ielts.model.Vocabulary;
import com.ddhva.ielts.repositories.TopicRepository;
import com.ddhva.ielts.repositories.VocabularyRepository;
import com.ddhva.ielts.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;


    @Override
    public Pagination<VocabularyResponse> getVocabularyByTopicId(String topicId, int page, int size) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            UUID topicUUID = UUID.fromString(topicId);
            Page<Vocabulary> vocabularies = vocabularyRepository.findByTopic_Id(topicUUID, pageable);
            return getVocabularyResponsePagination(vocabularies, page, size);
        }catch (Exception e){
            log.error("Error while fetching vocabularies from database", e);
            throw new RuntimeException("Error while fetching vocabularies from database");
        }
    }

    @Override
    public Pagination<VocabularyResponse> searchVocabulary(String word, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Vocabulary> vocabularies = vocabularyRepository.searchWord(word, pageable)
                    .orElseThrow(() -> new IllegalArgumentException("No vocabulary found"));
            return getVocabularyResponsePagination(vocabularies, page, size);
        }catch (Exception e){
            log.error("Error while fetching vocabularies from database", e);
            throw new RuntimeException("Error while fetching vocabularies from database");
        }
    }


    private Pagination<VocabularyResponse> getVocabularyResponsePagination(Page<Vocabulary> vocabularies, int page, int size) {
        List<VocabularyResponse> responses = vocabularies.stream()
                .map(vocabulary -> {
                    VocabularyResponse res = modelMapper.map(vocabulary, VocabularyResponse.class);
                    if (vocabulary.getTopic() != null) {
                        res.setTopicId(vocabulary.getTopic().getId().toString());
                    }
                    return res;
                })
                .collect(Collectors.toList());
        log.info("Successfully fetched {} vocabularies", vocabularies.getTotalElements());
        Pagination<VocabularyResponse> pagination = new Pagination<>();
        pagination.setPage(page);
        pagination.setSize(size);
        pagination.setTotalElements(vocabularies.getTotalElements());
        pagination.setTotalPages(vocabularies.getTotalPages());
        pagination.setContent(responses);
        return pagination;
    }

    @Override
    public VocabularyResponse getVocabularyById(String id) {
        try{
            UUID vocabularyUUID = UUID.fromString(id);
            Vocabulary vocabulary = vocabularyRepository.findById(vocabularyUUID)
                    .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
            VocabularyResponse res = modelMapper.map(vocabulary, VocabularyResponse.class);
            if (vocabulary.getTopic() != null) {
                res.setTopicId(vocabulary.getTopic().getId().toString());
            }
            log.info("Successfully mapped {} vocabulary to VocabularyResponse", res.getId());
            return res;
        }catch (Exception e){
            log.error("Error while fetching vocabulary from database", e);
            throw new RuntimeException("Error while fetching vocabulary from database");
        }
    }

    @Override
    public VocabularyResponse updateVocabulary(String id, VocabularyRequest request) {
        try{
            UUID vocabularyUUID = UUID.fromString(id);
            UUID topicUUID = UUID.fromString(request.getTopicId());
            Vocabulary vocabulary = vocabularyRepository.findById(vocabularyUUID)
                    .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
            Topic topic = topicRepository.findById(topicUUID)
                    .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
            modelMapper.map(request, vocabulary);
            vocabulary.setUpdatedAt(Instant.now());
            VocabularyResponse res = modelMapper.map(vocabulary, VocabularyResponse.class);
            if (vocabulary.getTopic() != null) {
                res.setTopicId(vocabulary.getTopic().getId().toString());
            }
            vocabulary.setTopic(topic);
            vocabulary = vocabularyRepository.save(vocabulary);
            log.info("Successfully updated vocabulary {}", vocabulary.getId());
            return res;
        }catch (Exception e){
            log.error("Error while updating vocabulary", e);
            throw new RuntimeException("Error while updating vocabulary");
        }
    }

    @Override
    @Transactional
    public void importExcel(MultipartFile file, String topicId) {
        try{
            UUID topicUUID = UUID.fromString(topicId);
            Topic topic = topicRepository.findById(topicUUID)
                    .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            List<Vocabulary> vocabularies = new ArrayList<>();
            for(Row cells : sheet){
                if(cells.getRowNum() == 0) continue;
                Vocabulary vocabulary = Vocabulary.builder().build();
                vocabulary.setWord(cells.getCell(1).getStringCellValue());
                vocabulary.setIpa(cells.getCell(2).getStringCellValue());
                vocabulary.setPart_of_speech(cells.getCell(3).getStringCellValue());
                vocabulary.setDefinition(cells.getCell(4).getStringCellValue());
                vocabulary.setExample(cells.getCell(5).getStringCellValue());
                vocabulary.setAudio_url(cells.getCell(6).getStringCellValue());
                vocabulary.setTopic(topic);
                vocabulary.setStatus(VocabularyStatus.ACTIVE);
                vocabularies.add(vocabulary);
            }
            log.info("Successfully imported {} file excel", vocabularies.size());
            vocabularyRepository.saveAll(vocabularies);
        }catch (Exception e){
            log.error("Error while importing excel file", e);
            throw new RuntimeException("Error while importing excel file");
        }
    }

    @Override
    public void deleteVocabulary(String id) {
        UUID vocabularyUUID = UUID.fromString(id);
        Vocabulary vocabulary = vocabularyRepository.findById(vocabularyUUID)
                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
        vocabulary.setStatus(VocabularyStatus.INACTIVE);
        vocabulary.setDeletedAt(Instant.now());
        vocabularyRepository.save(vocabulary);
        log.info("Successfully deleted vocabulary {}", vocabulary.getId());
    }
}
