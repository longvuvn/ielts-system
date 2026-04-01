package com.ddhva.ielts.deckvocabulary;

import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyRequest;
import com.ddhva.ielts.dto.deckvocabulary.req.DeckVocabularyUpdateRequest;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.dictionary.DictionaryApiResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
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
import com.ddhva.ielts.service.DictionaryService;
import com.ddhva.ielts.service.VocabularyService;
import com.ddhva.ielts.service.impl.DeckVocabularyServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckVocabularyServiceTest {

    @Mock private DeckVocabularyRepository deckVocabularyRepository;
    @Mock private VocabularyRepository vocabularyRepository;
    @Mock private FlashcardRepository flashcardRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private VocabularyService vocabularyService;
    @Mock private DictionaryService dictionaryService;
    @Mock private TopicRepository topicRepository;

    @InjectMocks
    private DeckVocabularyServiceImpl deckVocabularyService;

    @Test
    @DisplayName("Should get all deck vocabularies by flashcard id")
    void testGetAllDeckVocabularyByFlashcardId() {
        UUID flashcardId = UUID.randomUUID();
        UUID vocabId = UUID.randomUUID();

        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(vocabId);
        vocabulary.setWord("apple");
        vocabulary.setIpa("/ˈæp.əl/");
        vocabulary.setDefinition("a fruit");
        vocabulary.setExample("I eat an apple.");
        vocabulary.setAudio_url("audio-url");

        DeckVocabulary deck = new DeckVocabulary();
        deck.setId(UUID.randomUUID());
        deck.setFlashcard(flashcard);
        deck.setVocabulary(vocabulary);
        deck.setReviewStatus(ReviewStatus.NEW);
        deck.setLastReviewedAt(Instant.now());

        Page<DeckVocabulary> pageData = new PageImpl<>(List.of(deck), PageRequest.of(0, 10), 1);

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(deckVocabularyRepository.findByFlashcard_Id(eq(flashcardId), any())).thenReturn(Optional.of(pageData));
        when(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocabulary));
        when(modelMapper.map(any(DeckVocabulary.class), eq(DeckVocabularyResponse.class)))
                .thenReturn(new DeckVocabularyResponse());

        Pagination<DeckVocabularyResponse> result =
                deckVocabularyService.getAllDeckVocabularyByFlashcardId(flashcardId.toString(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getContent().size());
        assertEquals("apple", result.getContent().get(0).getWord());
        assertEquals("NEW", result.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("Should create deck vocabulary with existing vocabularyId")
    void testCreateDeckVocabulary_WithVocabularyId() {
        UUID flashcardId = UUID.randomUUID();
        UUID vocabId = UUID.randomUUID();

        DeckVocabularyRequest request = new DeckVocabularyRequest();
        request.setFlashcardId(flashcardId.toString());
        request.setVocabularyId(vocabId.toString());

        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(vocabId);

        DeckVocabulary mapped = new DeckVocabulary();
        mapped.setId(UUID.randomUUID());

        when(modelMapper.map(request, DeckVocabulary.class)).thenReturn(mapped);
        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocabulary));

        deckVocabularyService.createDeckVocabulary(request);

        ArgumentCaptor<DeckVocabulary> captor = ArgumentCaptor.forClass(DeckVocabulary.class);
        verify(deckVocabularyRepository, times(1)).save(captor.capture());

        DeckVocabulary saved = captor.getValue();
        assertEquals(flashcardId, saved.getFlashcard().getId());
        assertEquals(vocabId, saved.getVocabulary().getId());
        assertEquals(ReviewStatus.NEW, saved.getReviewStatus());
        assertNotNull(saved.getLastReviewedAt());
    }

    @Test
    @DisplayName("Should create deck vocabulary without vocabularyId and create vocabulary from dictionary")
    void testCreateDeckVocabulary_WithoutVocabularyId() {
        UUID flashcardId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        UUID newVocabId = UUID.randomUUID();

        DeckVocabularyRequest request = new DeckVocabularyRequest();
        request.setFlashcardId(flashcardId.toString());
        request.setWord("banana");

        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setName("USER-CUSTOM");

        DictionaryApiResponse api = new DictionaryApiResponse();
        api.setIpa("/bəˈnɑː.nə/");
        api.setDefinition("a yellow fruit");
        api.setExample("Banana is yellow.");
        api.setAudioUrl("audio-url");
        api.setPartOfSpeech("noun");

        VocabularyResponse created = new VocabularyResponse();
        created.setId(newVocabId.toString());

        Vocabulary newVocab = new Vocabulary();
        newVocab.setId(newVocabId);

        DeckVocabulary mapped = new DeckVocabulary();
        mapped.setId(UUID.randomUUID());

        when(modelMapper.map(request, DeckVocabulary.class)).thenReturn(mapped);
        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(topicRepository.findByName("USER-CUSTOM")).thenReturn(Optional.of(topic));
        when(dictionaryService.lookupWord("banana")).thenReturn(api);
        when(vocabularyService.createVocabulary(any())).thenReturn(created);
        when(vocabularyRepository.findById(newVocabId)).thenReturn(Optional.of(newVocab));

        deckVocabularyService.createDeckVocabulary(request);

        ArgumentCaptor<DeckVocabulary> captor = ArgumentCaptor.forClass(DeckVocabulary.class);
        verify(deckVocabularyRepository).save(captor.capture());
        assertEquals(newVocabId, captor.getValue().getVocabulary().getId());
        assertEquals(flashcardId, captor.getValue().getFlashcard().getId());
    }

    @Test
    @DisplayName("Should get deck vocabulary by id")
    void testGetDeckVocabularyById() {
        UUID id = UUID.randomUUID();

        DeckVocabulary deck = new DeckVocabulary();
        deck.setId(id);

        DeckVocabularyResponse response = new DeckVocabularyResponse();
        response.setId(id.toString());

        when(deckVocabularyRepository.findById(id)).thenReturn(Optional.of(deck));
        when(modelMapper.map(deck, DeckVocabularyResponse.class)).thenReturn(response);

        DeckVocabularyResponse result = deckVocabularyService.getDeckVocabularyById(id.toString());

        assertNotNull(result);
        assertEquals(id.toString(), result.getId());
    }

    @Test
    @DisplayName("Should update deck vocabulary successfully")
    void testUpdateDeckVocabulary() {
        UUID deckId = UUID.randomUUID();
        UUID vocabId = UUID.randomUUID();
        UUID flashcardId = UUID.randomUUID();

        DeckVocabularyUpdateRequest request = new DeckVocabularyUpdateRequest();
        request.setVocabularyId(vocabId.toString());
        request.setFlashcardId(flashcardId.toString());

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(vocabId);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);

        DeckVocabulary deck = new DeckVocabulary();
        deck.setId(deckId);

        doAnswer(inv -> inv.getArgument(1))
                .when(modelMapper).map(any(DeckVocabularyUpdateRequest.class), any(DeckVocabulary.class));

        when(vocabularyRepository.findById(vocabId)).thenReturn(Optional.of(vocabulary));
        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));
        when(deckVocabularyRepository.findById(deckId)).thenReturn(Optional.of(deck));
        when(deckVocabularyRepository.save(any(DeckVocabulary.class))).thenAnswer(inv -> inv.getArgument(0));
        when(modelMapper.map(any(DeckVocabulary.class), eq(DeckVocabularyResponse.class)))
                .thenReturn(new DeckVocabularyResponse());

        DeckVocabularyResponse result = deckVocabularyService.updateDeckVocabulary(deckId.toString(), request);

        assertNotNull(result);
        verify(deckVocabularyRepository, times(1)).save(any(DeckVocabulary.class));
    }

    @Test
    @DisplayName("Should delete deck vocabulary successfully")
    void testDeleteDeckVocabulary() {
        UUID id = UUID.randomUUID();
        DeckVocabulary deck = new DeckVocabulary();
        deck.setId(id);

        when(deckVocabularyRepository.findById(id)).thenReturn(Optional.of(deck));

        deckVocabularyService.deleteDeckVocabulary(id.toString());

        verify(deckVocabularyRepository, times(1)).delete(deck);
    }
}