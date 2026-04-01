package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyUpdateRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.ReviewRequest;
import com.ddhva.ielts.dto.deckvocabulary.res.AnswerDefinition;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.dictionary.DictionaryApiResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import com.ddhva.ielts.enums.ReviewStatus;
import com.ddhva.ielts.model.DeckVocabulary;
import com.ddhva.ielts.model.Flashcard;
import com.ddhva.ielts.model.Topic;
import com.ddhva.ielts.model.Vocabulary;
import com.ddhva.ielts.repositories.DeckVocabularyRepository;
import com.ddhva.ielts.repositories.FlashcardRepository;
import com.ddhva.ielts.repositories.TopicRepository;
import com.ddhva.ielts.repositories.VocabularyRepository;
import com.ddhva.ielts.service.DeckVocabularyService;
import com.ddhva.ielts.service.DictionaryService;
import com.ddhva.ielts.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeckVocabularyServiceImpl implements DeckVocabularyService {

        private final DeckVocabularyRepository deckVocabularyRepository;
        private final VocabularyRepository vocabularyRepository;
        private final FlashcardRepository flashcardRepository;
        private final ModelMapper modelMapper;
        private final VocabularyService vocabularyService;
        private final DictionaryService dictionaryService;
        private final TopicRepository topicRepository;


        @Override
        public Pagination<DeckVocabularyResponse> getAllDeckVocabularyByFlashcardId(String flashcardId, int page,
                        int size) {
                Pageable pageable = PageRequest.of(page, size);
                UUID flashcardUUID = UUID.fromString(flashcardId);
                Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
                Page<DeckVocabulary> deckVocabularies = deckVocabularyRepository
                                .findByFlashcard_Id(flashcard.getId(), pageable)
                                .orElseThrow(() -> new IllegalArgumentException("No deck vocabulary found"));
                List<DeckVocabularyResponse> responses = deckVocabularies.stream()
                                .map(this::getDeckVocabularyResponse)
                                .collect(Collectors.toList());
                Pagination<DeckVocabularyResponse> pagination = new Pagination<>();
                pagination.setPage(page);
                pagination.setSize(size);
                pagination.setTotalElements(deckVocabularies.getTotalElements());
                pagination.setTotalPages(deckVocabularies.getTotalPages());
                pagination.setContent(responses);
                return pagination;
        }

        @Override
        public void createDeckVocabulary(DeckVocabularyRequest request) {
                UUID flashcardUUID = UUID.fromString(request.getFlashcardId());
                DeckVocabulary deckVocabulary = modelMapper.map(request, DeckVocabulary.class);
                Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
                if (request.getVocabularyId() != null&& !request.getVocabularyId().isEmpty()) {
                        UUID vocabUUID = UUID.fromString(request.getVocabularyId());
                        Vocabulary vocabulary = vocabularyRepository.findById(vocabUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
                        deckVocabulary.setVocabulary(vocabulary);
                }else {
                        Topic topic = topicRepository.findByName("USER-CUSTOM")
                                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
                        DictionaryApiResponse apiResponse = dictionaryService.lookupWord(request.getWord());
                        VocabularyRequest req = getVocabularyRequest(request, topic, apiResponse);
                        VocabularyResponse response = vocabularyService.createVocabulary(req);
                        Vocabulary newVocab = vocabularyRepository.findById(UUID.fromString(response.getId()))
                                .orElseThrow(() -> new IllegalArgumentException("Failed to create vocabulary"));

                        deckVocabulary.setVocabulary(newVocab);
                }
                deckVocabulary.setFlashcard(flashcard);
                deckVocabulary.setReviewStatus(ReviewStatus.NEW);
                deckVocabulary.setLastReviewedAt(Instant.now());
                deckVocabularyRepository.save(deckVocabulary);
                log.info("Successfully created deck vocabulary {}", deckVocabulary.getId());
        }

        private VocabularyRequest getVocabularyRequest(DeckVocabularyRequest request, Topic topic, DictionaryApiResponse apiResponse) {
                VocabularyRequest req = new VocabularyRequest();
                req.setWord(request.getWord());
                req.setTopicId(topic.getId().toString());
                if (apiResponse != null){
                        req.setIpa(apiResponse.getIpa());
                        req.setDefinition(apiResponse.getDefinition());
                        req.setExample(apiResponse.getExample());
                        req.setAudio_url(apiResponse.getAudioUrl());
                        req.setPart_of_speech(apiResponse.getPartOfSpeech());
                }else {
                        req.setIpa(request.getIpa());
                        req.setDefinition(request.getDefinition());
                        req.setExample(request.getExample());
                        req.setAudio_url(request.getAudio_url());
                        req.setPart_of_speech(request.getPart_of_speech());
                }
                return req;
        }

        @Override
        public DeckVocabularyResponse getDeckVocabularyById(String deckVocabularyId) {
                UUID deckVocabularyUUID = UUID.fromString(deckVocabularyId);
                DeckVocabulary deckVocabulary = deckVocabularyRepository.findById(deckVocabularyUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Deck vocabulary not found"));
                log.info("Successfully fetched deck vocabulary {}", deckVocabulary.getId());
                return getDeckVocabularyResponse(deckVocabulary);
        }


        @Override
        public DeckVocabularyResponse updateDeckVocabulary(String deckVocabularyId, DeckVocabularyUpdateRequest request) {
                UUID deckVocabularyUUID = UUID.fromString(deckVocabularyId);
                UUID vocabularyUUID = UUID.fromString(request.getVocabularyId());
                UUID flashcardUUID = UUID.fromString(request.getFlashcardId());
                Vocabulary vocabulary = vocabularyRepository.findById(vocabularyUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
                Flashcard flashcard = flashcardRepository.findById(flashcardUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Flashcard not found"));
                DeckVocabulary deckVocabulary = deckVocabularyRepository.findById(deckVocabularyUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Deck vocabulary not found"));
                modelMapper.map(request, deckVocabulary);
                deckVocabulary.setVocabulary(vocabulary);
                deckVocabulary.setFlashcard(flashcard);
                deckVocabulary = deckVocabularyRepository.save(deckVocabulary);
                log.info("Successfully updated deck vocabulary {}", deckVocabulary.getId());
                return getDeckVocabularyResponse(deckVocabulary);
        }

        @Override
        public void deleteDeckVocabulary(String deckVocabularyId) {
                UUID deckVocabularyUUID = UUID.fromString(deckVocabularyId);
                DeckVocabulary deckVocabulary = deckVocabularyRepository.findById(deckVocabularyUUID)
                                .orElseThrow(() -> new IllegalArgumentException("Deck vocabulary not found"));
                deckVocabularyRepository.delete(deckVocabulary);
                log.info("Successfully deleted deck vocabulary {}", deckVocabulary.getId());
        }

        @Override
        public DeckVocabularyResponse countDeckVocabularyByFlashcardId(String deckVocabularyId) {
                UUID deckVocabularyUUID = UUID.fromString(deckVocabularyId);
                DeckVocabulary deckVocabulary = deckVocabularyRepository.findById(deckVocabularyUUID)
                        .orElseThrow(() -> new IllegalArgumentException("Deck vocabulary not found"));
                deckVocabulary.setReviewCount(deckVocabulary.getReviewCount() + 1);
                deckVocabulary = deckVocabularyRepository.save(deckVocabulary);
                return getDeckVocabularyResponse(deckVocabulary);
        }

        @Override
        public List<AnswerDefinition> userDefinition(String deckVocabularyId) {
                UUID deckVocabularyUUID = UUID.fromString(deckVocabularyId);

                DeckVocabulary correct = deckVocabularyRepository.findById(deckVocabularyUUID)
                        .orElseThrow(() -> new IllegalArgumentException("DeckVocabulary not found"));

                List<DeckVocabulary> wrongs = deckVocabularyRepository
                        .findWrongAnswers(correct.getFlashcard().getId(), correct.getVocabulary().getId());
                Collections.shuffle(wrongs);
                List<DeckVocabulary> randomThree = wrongs.stream().limit(3).toList();

                List<AnswerDefinition> answers = new ArrayList<>();
                answers.add(AnswerDefinition.builder()
                        .vocabularyId(correct.getId().toString())
                        .definition(correct.getUserDefinition())
                        .isCorrect(String.valueOf(true))
                        .build());

                randomThree.forEach(dv -> answers.add(AnswerDefinition.builder()
                        .vocabularyId(dv.getId().toString())
                        .definition(dv.getUserDefinition())
                        .isCorrect(String.valueOf(false))
                        .build()));

                Collections.shuffle(answers);
                return answers;
        }

        @Override
        public void review(String id, ReviewRequest request) {
                UUID deckVocabularyUUID = UUID.fromString(id);
                DeckVocabulary deckVocabulary = deckVocabularyRepository.findById(deckVocabularyUUID)
                        .orElseThrow(() -> new IllegalArgumentException("Deck vocabulary not found"));
                modelMapper.map(request, deckVocabulary);
                deckVocabulary.setReviewCount(deckVocabulary.getReviewCount() + 1);
                deckVocabulary.setLastReviewedAt(Instant.now());
                if (Boolean.parseBoolean(request.getIsCorrect())) {
                        int count = deckVocabulary.getReviewCount();
                        if (count >= 3) {
                                deckVocabulary.setReviewStatus(ReviewStatus.MASTERED);
                        } else {
                                deckVocabulary.setReviewStatus(ReviewStatus.LEARNING);
                        }
                } else {
                        deckVocabulary.setReviewStatus(ReviewStatus.LEARNING);
                }
                deckVocabularyRepository.save(deckVocabulary);
        }

        private DeckVocabularyResponse getDeckVocabularyResponse(DeckVocabulary deckVocabulary) {
                DeckVocabularyResponse res = modelMapper.map(deckVocabulary, DeckVocabularyResponse.class);
                if (deckVocabulary.getVocabulary() != null) {
                        Vocabulary vocabulary = vocabularyRepository.findById(deckVocabulary.getVocabulary().getId())
                                .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
                        res.setFlashcardId(deckVocabulary.getFlashcard().getId().toString());
                        res.setVocabularyId(vocabulary.getId().toString());
                        res.setWord(vocabulary.getWord());
                        res.setIpa(vocabulary.getIpa());
                        res.setExample(vocabulary.getExample());
                        res.setDefinition(vocabulary.getDefinition());
                        res.setAudioUrl(vocabulary.getAudio_url());
                        res.setStatus(deckVocabulary.getReviewStatus().toString());
                        res.setLastReviewed(deckVocabulary.getLastReviewedAt().toString());
                }
                return res;
        }
}
