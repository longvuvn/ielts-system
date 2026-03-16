package com.ddhva.ielts.deckvocabulary;

import com.ddhva.ielts.controller.DeckVocabularyController;
import com.ddhva.ielts.dto.deckvocabulary.res.DeckVocabularyResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.DeckVocabularyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeckVocabularyController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeckVocabularyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeckVocabularyService deckVocabularyService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Should get all deck vocabularies by flashcard id")
    void testGetAllByFlashcardId() throws Exception {
        String flashcardId = UUID.randomUUID().toString();

        DeckVocabularyResponse item = new DeckVocabularyResponse();
        item.setId(UUID.randomUUID().toString());
        item.setFlashcardId(flashcardId);
        item.setWord("apple");

        Pagination<DeckVocabularyResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(item));

        when(deckVocabularyService.getAllDeckVocabularyByFlashcardId(flashcardId, 0, 10))
                .thenReturn(pagination);

        mockMvc.perform(get("/api/v1/deck-vocabulary/flashcard/{flashcardId}", flashcardId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get All Successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].word").value("apple"));
    }

    @Test
    @DisplayName("Should get deck vocabulary by id")
    void testGetById() throws Exception {
        String id = UUID.randomUUID().toString();

        DeckVocabularyResponse response = new DeckVocabularyResponse();
        response.setId(id);
        response.setWord("banana");

        when(deckVocabularyService.getDeckVocabularyById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/deck-vocabulary/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get Successfully"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.word").value("banana"));
    }

    @Test
    @DisplayName("Should create deck vocabulary successfully")
    void testCreate() throws Exception {
        doNothing().when(deckVocabularyService).createDeckVocabulary(any());

        String json = """
                {
                  "flashcardId": "%s",
                  "word": "apple"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/deck-vocabulary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Create Successfully"));
    }

    @Test
    @DisplayName("Should update deck vocabulary successfully")
    void testUpdate() throws Exception {
        String id = UUID.randomUUID().toString();

        DeckVocabularyResponse response = new DeckVocabularyResponse();
        response.setId(id);
        response.setWord("updated-word");

        when(deckVocabularyService.updateDeckVocabulary(eq(id), any())).thenReturn(response);

        String json = """
                {
                  "flashcardId": "%s",
                  "vocabularyId": "%s",
                  "word": "updated-word"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(put("/api/v1/deck-vocabulary/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update Successfully"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.word").value("updated-word"));
    }

    @Test
    @DisplayName("Should delete deck vocabulary successfully")
    void testDelete() throws Exception {
        String id = UUID.randomUUID().toString();

        doNothing().when(deckVocabularyService).deleteDeckVocabulary(id);

        mockMvc.perform(delete("/api/v1/deck-vocabulary/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete Successfully"));
    }
}