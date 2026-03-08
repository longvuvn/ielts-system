package com.ddhva.ielts.vocabulary;

import com.ddhva.ielts.controller.VocabularyController;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.dto.vocabulary.req.VocabularyRequest;
import com.ddhva.ielts.dto.vocabulary.res.VocabularyResponse;
import com.ddhva.ielts.service.VocabularyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VocabularyController.class)
@AutoConfigureMockMvc(addFilters = false)
public class VocabularyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VocabularyService vocabularyService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return vocabulary list by topic id")
    public void testGetAllByTopicId() throws Exception {
        String topicId = UUID.randomUUID().toString();

        VocabularyResponse response = new VocabularyResponse();
        response.setId(UUID.randomUUID().toString());
        response.setTopicId(topicId);
        response.setWord("apple");
        response.setIpa("/ˈæp.əl/");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setDefinition("a fruit");
        response.setPart_of_speech("noun");

        Pagination<VocabularyResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(response));

        when(vocabularyService.getVocabularyByTopicId(topicId, 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/vocabularies/topic/{topicId}", topicId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get All By TopicId Successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].word").value("apple"))
                .andExpect(jsonPath("$.data.content[0].topicId").value(topicId));
    }

    @Test
    @DisplayName("Should search vocabulary successfully")
    public void testSearchVocabulary() throws Exception {
        String word = "apple";
        String topicId = UUID.randomUUID().toString();
        String vocabularyId = UUID.randomUUID().toString();

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId);
        response.setTopicId(topicId);
        response.setWord(word);
        response.setIpa("/ˈæp.əl/");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setDefinition("a fruit");
        response.setPart_of_speech("noun");

        Pagination<VocabularyResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(response));

        when(vocabularyService.searchVocabulary(word, 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/vocabularies/search")
                        .param("word", word)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Search Successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(vocabularyId))
                .andExpect(jsonPath("$.data.content[0].word").value(word));
    }

    @Test
    @DisplayName("Should return vocabulary by id")
    public void testGetVocabularyById() throws Exception {
        String topicId = UUID.randomUUID().toString();
        String vocabularyId = UUID.randomUUID().toString();

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId);
        response.setTopicId(topicId);
        response.setWord("apple");
        response.setIpa("/ˈæp.əl/");
        response.setExample("I eat an apple every day.");
        response.setAudio_url("audio-url");
        response.setDefinition("a fruit");
        response.setPart_of_speech("noun");
        response.setStatus("ACTIVE");

        when(vocabularyService.getVocabularyById(vocabularyId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/vocabularies/{id}", vocabularyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get Successfully"))
                .andExpect(jsonPath("$.data.id").value(vocabularyId))
                .andExpect(jsonPath("$.data.topicId").value(topicId))
                .andExpect(jsonPath("$.data.word").value("apple"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should import excel successfully")
    public void testImportExcel() throws Exception {
        String topicId = UUID.randomUUID().toString();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "vocabulary.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "fake excel content".getBytes()
        );

        doNothing().when(vocabularyService).importExcel(file, topicId);

        mockMvc.perform(multipart("/api/v1/vocabularies")
                        .file(file)
                        .param("topicId", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Successfully imported"));
    }

    @Test
    @DisplayName("Should update vocabulary successfully")
    public void testUpdateVocabulary() throws Exception {
        String vocabularyId = UUID.randomUUID().toString();
        String topicId = UUID.randomUUID().toString();

        VocabularyRequest request = new VocabularyRequest();
        request.setTopicId(topicId);
        request.setWord("banana");
        request.setIpa("/bəˈnɑː.nə/");
        request.setDefinition("a yellow fruit");
        request.setExample("Banana is yellow.");
        request.setPart_of_speech("noun");
        request.setAudio_url("new-audio");

        VocabularyResponse response = new VocabularyResponse();
        response.setId(vocabularyId);
        response.setTopicId(topicId);
        response.setWord("banana");
        response.setIpa("/bəˈnɑː.nə/");
        response.setDefinition("a yellow fruit");
        response.setExample("Banana is yellow.");
        response.setPart_of_speech("noun");
        response.setAudio_url("new-audio");
        response.setStatus("ACTIVE");

        when(vocabularyService.updateVocabulary(vocabularyId, request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/vocabularies/{id}", vocabularyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update Successfully"))
                .andExpect(jsonPath("$.data.id").value(vocabularyId))
                .andExpect(jsonPath("$.data.topicId").value(topicId))
                .andExpect(jsonPath("$.data.word").value("banana"))
                .andExpect(jsonPath("$.data.definition").value("a yellow fruit"));
    }

    @Test
    @DisplayName("Should delete vocabulary successfully")
    public void testDeleteVocabulary() throws Exception {
        String vocabularyId = UUID.randomUUID().toString();

        doNothing().when(vocabularyService).deleteVocabulary(vocabularyId);

        mockMvc.perform(delete("/api/v1/vocabularies/{id}", vocabularyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete Successfully"));
    }
}