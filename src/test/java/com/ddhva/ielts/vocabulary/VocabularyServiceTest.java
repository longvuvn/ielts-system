package com.ddhva.ielts.vocabulary;

import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import com.ddhva.ielts.enums.VocabularyStatus;
import com.ddhva.ielts.repositories.TopicRepository;
import com.ddhva.ielts.repositories.VocabularyRepository;
import com.ddhva.ielts.service.impl.VocabularyServiceImpl;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;
import org.apache.poi.ss.usermodel.*;
import com.ddhva.ielts.model.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.io.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class VocabularyServiceTest {

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private VocabularyServiceImpl vocabularyService;

    @Captor
    private ArgumentCaptor<List<Vocabulary>> vocabularyListCaptor;

    @Test
    @DisplayName("Should return vocabulary list by topic id")
    public void testGetVocabularyByTopicId() {
        UUID topicId = UUID.randomUUID();
        UUID vocabularyId = UUID.randomUUID();

        Topic topic = new Topic();
        topic.setId(topicId);

        Vocabulary vocabulary = Vocabulary.builder()
                .id(vocabularyId)
                .word("apple")
                .ipa("/ˈæp.əl/")
                .definition("a fruit")
                .example("I eat an apple every day.")
                .audio_url("audio-url")
                .part_of_speech("noun")
                .status(VocabularyStatus.ACTIVE)
                .topic(topic)
                .build();

        Page<Vocabulary> vocabularyPage = new PageImpl<>(List.of(vocabulary));

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId.toString());
        response.setTopicId(topicId.toString());
        response.setWord("apple");
        response.setIpa("/ˈæp.əl/");
        response.setDefinition("a fruit");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setPart_of_speech("noun");
        response.setStatus("ACTIVE");

        when(vocabularyRepository.findByTopic_Id(eq(topicId), any(Pageable.class)))
                .thenReturn(vocabularyPage);

        when(modelMapper.map(vocabulary, VocabularyResponse.class))
                .thenReturn(response);

        Pagination<VocabularyResponse> result =
                vocabularyService.getVocabularyByTopicId(topicId.toString(), 0, 10);

        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals("apple", result.getContent().getFirst().getWord());
        assertEquals(topicId.toString(), result.getContent().getFirst().getTopicId());
    }

    @Test
    @DisplayName("Should return topic Id")
    public void testGetVocabularyId(){
        UUID topicId = UUID.randomUUID();
        UUID vocabularyId = UUID.randomUUID();
        Topic topic = new Topic();
        topic.setId(topicId);

        Vocabulary vocabulary = Vocabulary.builder()
                .id(vocabularyId)
                .topic(topic)
                .word("apple")
                .ipa("/ˈæp.əl/")
                .definition("a fruit")
                .example("I eat an apple every day.")
                .part_of_speech("noun")
                .audio_url("audio-url")
                .status(VocabularyStatus.ACTIVE)
                .build();

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId.toString());
        response.setTopicId(topicId.toString());
        response.setWord("apple");
        response.setIpa("/ˈæp.əl/");
        response.setDefinition("a fruit");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setPart_of_speech("noun");
        response.setStatus("ACTIVE");

        when(vocabularyRepository.findById(eq(vocabularyId)))
                .thenReturn(Optional.of(vocabulary));
        when(modelMapper.map(vocabulary, VocabularyResponse.class))
                .thenReturn(response);

        VocabularyResponse result = vocabularyService.getVocabularyById(vocabularyId.toString());
        assertEquals(vocabularyId.toString(), result.getId());
        assertEquals(topicId.toString(), result.getTopicId());
        assertEquals("apple", result.getWord());
        assertEquals("/ˈæp.əl/", result.getIpa());
        assertEquals("noun", result.getPart_of_speech());
        assertEquals("audio-url", result.getAudio_url());
        assertEquals("I eat an apple every day.", result.getExample());
        assertEquals("a fruit", result.getDefinition());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    @DisplayName("Should return word")
    public void testSearchVocabulary(){
        String word = "apple";
        UUID topicId = UUID.randomUUID();
        UUID vocabularyId = UUID.randomUUID();
        Topic topic = new Topic();
        topic.setId(topicId);

        Vocabulary vocabulary = Vocabulary.builder()
                .id(vocabularyId)
                .topic(topic)
                .word(word)
                .ipa("/ˈæp.əl/")
                .definition("a fruit")
                .example("I eat an apple every day.")
                .part_of_speech("noun")
                .audio_url("audio-url")
                .status(VocabularyStatus.ACTIVE)
                .build();

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId.toString());
        response.setTopicId(topicId.toString());
        response.setWord(word);
        response.setIpa("/ˈæp.əl/");
        response.setDefinition("a fruit");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setPart_of_speech("noun");
        response.setStatus("ACTIVE");

        when(vocabularyRepository.searchWord(eq(word), any(Pageable.class)))
                .thenReturn(Optional.of(new PageImpl<>(List.of(vocabulary))));

        when(modelMapper.map(vocabulary, VocabularyResponse.class))
                .thenReturn(response);

        Pagination<VocabularyResponse> result = vocabularyService.searchVocabulary(word, 0, 10);
        assertEquals(1, result.getContent().size());
        assertEquals(word, result.getContent().getFirst().getWord());
        assertEquals(topicId.toString(), result.getContent().getFirst().getTopicId());
        assertEquals(vocabularyId.toString(), result.getContent().getFirst().getId());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
    }


    private MultipartFile createMockExcelFile() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Vocabulary");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("No");
        header.createCell(1).setCellValue("Word");
        header.createCell(2).setCellValue("IPA");
        header.createCell(3).setCellValue("Part of speech");
        header.createCell(4).setCellValue("Definition");
        header.createCell(5).setCellValue("Example");
        header.createCell(6).setCellValue("Audio URL");


        Row dataRow = sheet.createRow(1);
        dataRow.createCell(0).setCellValue(1);
        dataRow.createCell(1).setCellValue("apple");
        dataRow.createCell(2).setCellValue("/ˈæp.əl/");
        dataRow.createCell(3).setCellValue("noun");
        dataRow.createCell(4).setCellValue("a fruit");
        dataRow.createCell(5).setCellValue("I eat an apple every day.");
        dataRow.createCell(6).setCellValue("audio-url");


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "vocabulary.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                outputStream.toByteArray()
        );
    }

    @Test
    @DisplayName("Should return completed when import excel successfully")
    public void testImportExcel() throws IOException {
        MultipartFile file = createMockExcelFile();
        UUID topicId = UUID.randomUUID();

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setName("IELTS - Topic");

        when(topicRepository.findById(eq(topicId)))
                .thenReturn(Optional.of(topic));

        vocabularyService.importExcel(file, topicId.toString());
        verify(vocabularyRepository).saveAll(vocabularyListCaptor.capture());
        List<Vocabulary> savedVocabularies = vocabularyListCaptor.getValue();

        assertEquals(1, savedVocabularies.size());
        Vocabulary savedVocabulary = savedVocabularies.getFirst();
        assertEquals("apple", savedVocabulary.getWord());
        assertEquals("/ˈæp.əl/", savedVocabulary.getIpa());
        assertEquals("noun", savedVocabulary.getPart_of_speech());
        assertEquals("a fruit", savedVocabulary.getDefinition());
        assertEquals("I eat an apple every day.", savedVocabulary.getExample());
        assertEquals("audio-url", savedVocabulary.getAudio_url());
        assertEquals(VocabularyStatus.ACTIVE, savedVocabulary.getStatus());
        assertEquals(topicId, savedVocabulary.getTopic().getId());
    }

    @Test
    @DisplayName("Should return error when import excel failed")
    public void testImportExcelFailed() throws IOException {
        MultipartFile file = createMockExcelFile();
        UUID topicId = UUID.randomUUID();

        when(topicRepository.findById(topicId))
                .thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> vocabularyService.importExcel(file, topicId.toString())
        );

        assertEquals("Error while importing excel file", exception.getMessage());
        verify(vocabularyRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should return deleted successfully")
    public void testDeleteVocabulary() throws IllegalArgumentException{
        UUID vocabularyId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();
        Topic topic = new Topic();
        topic.setId(topicId);

        Vocabulary vocabulary = Vocabulary.builder()
                .id(vocabularyId)
                .topic(topic)
                .word("apple")
                .ipa("/ˈæp.əl/")
                .definition("a fruit")
                .example("I eat an apple every day.")
                .part_of_speech("noun")
                .audio_url("audio-url")
                .status(VocabularyStatus.ACTIVE)
                .build();

        when(vocabularyRepository.findById(eq(vocabularyId)))
                .thenReturn(Optional.of(vocabulary));

        vocabularyService.deleteVocabulary(vocabularyId.toString());

        verify(vocabularyRepository).findById(eq(vocabularyId));
        verify(vocabularyRepository).save(vocabulary);

        assertEquals(VocabularyStatus.INACTIVE, vocabulary.getStatus());
        assertNotNull(vocabulary.getDeletedAt());
    }

    @Test
    @DisplayName("Should throw exception when vocabulary not found during delete")
    public void testDeleteVocabularyFailed() throws IllegalArgumentException{
        UUID vocabularyId = UUID.randomUUID();
        when(vocabularyRepository.findById(eq(vocabularyId)))
                .thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vocabularyService.deleteVocabulary(vocabularyId.toString())
        );
        assertEquals("Vocabulary not found", exception.getMessage());
        verify(vocabularyRepository, never()).save(any(Vocabulary.class));
    }

    @Test
    @DisplayName("Should return updated successfully")
    public void testUpdateVocabulary() {
        UUID vocabularyId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setName("New Topic");

        Vocabulary vocabulary = Vocabulary.builder()
                .id(vocabularyId)
                .word("apple")
                .ipa("/old/")
                .definition("old definition")
                .example("old example")
                .part_of_speech("noun")
                .audio_url("old-audio")
                .status(VocabularyStatus.ACTIVE)
                .build();

        VocabularyRequest request = new VocabularyRequest();
        request.setTopicId(topicId.toString());
        request.setWord("banana");
        request.setIpa("/bəˈnɑː.nə/");
        request.setDefinition("a yellow fruit");
        request.setExample("Banana is yellow.");
        request.setPart_of_speech("noun");
        request.setAudio_url("new-audio");
        request.setUpdatedAt(Instant.now().toString());

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId.toString());
        response.setTopicId(topicId.toString());
        response.setWord("banana");
        response.setIpa("/bəˈnɑː.nə/");
        response.setDefinition("a yellow fruit");
        response.setExample("Banana is yellow.");
        response.setPart_of_speech("noun");
        response.setAudio_url("new-audio");
        response.setStatus("ACTIVE");

        when(vocabularyRepository.findById(eq(vocabularyId)))
                .thenReturn(Optional.of(vocabulary));
        when(topicRepository.findById(eq(topicId)))
                .thenReturn(Optional.of(topic));

        doAnswer(invocation -> {
            VocabularyRequest source = invocation.getArgument(0);
            Vocabulary destination = invocation.getArgument(1);

            destination.setWord(source.getWord());
            destination.setIpa(source.getIpa());
            destination.setDefinition(source.getDefinition());
            destination.setExample(source.getExample());
            destination.setPart_of_speech(source.getPart_of_speech());
            destination.setAudio_url(source.getAudio_url());
            return null;
        }).when(modelMapper).map(eq(request), eq(vocabulary));

        when(modelMapper.map(vocabulary, VocabularyResponse.class))
                .thenReturn(response);

        when(vocabularyRepository.save(any(Vocabulary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VocabularyResponse result = vocabularyService.updateVocabulary(vocabularyId.toString(), request);

        verify(vocabularyRepository).findById(eq(vocabularyId));
        verify(topicRepository).findById(eq(topicId));
        verify(modelMapper).map(request, vocabulary);
        verify(vocabularyRepository).save(vocabulary);

        assertEquals(vocabularyId.toString(), result.getId());
        assertEquals(topicId.toString(), result.getTopicId());
        assertEquals("banana", result.getWord());
        assertEquals("/bəˈnɑː.nə/", result.getIpa());
        assertEquals("a yellow fruit", result.getDefinition());
        assertEquals("Banana is yellow.", result.getExample());
        assertEquals("noun", result.getPart_of_speech());
        assertEquals("new-audio", result.getAudio_url());

        assertEquals(topic, vocabulary.getTopic());
        assertEquals("banana", vocabulary.getWord());
        assertEquals("/bəˈnɑː.nə/", vocabulary.getIpa());
        assertNotNull(vocabulary.getUpdatedAt());
    }
}