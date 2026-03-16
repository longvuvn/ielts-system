package com.ddhva.ielts.flashcard;

import com.ddhva.ielts.controller.FlashcardController;
import com.ddhva.ielts.dto.flashcard.res.FlashcardResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.FlashcardService;
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

@WebMvcTest(FlashcardController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlashcardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlashcardService flashcardService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Should return flashcards by library id")
    void testGetAllByLibraryId() throws Exception {
        String libraryId = UUID.randomUUID().toString();

        FlashcardResponse item = new FlashcardResponse();
        item.setId(UUID.randomUUID().toString());

        Pagination<FlashcardResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(item));

        when(flashcardService.getAllFlashcardsByLibraryId(libraryId, 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/flashcard/library/{libraryId}", libraryId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get All Successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Should search flashcard successfully")
    void testSearchFlashcard() throws Exception {
        FlashcardResponse item = new FlashcardResponse();
        item.setId(UUID.randomUUID().toString());

        Pagination<FlashcardResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(item));

        when(flashcardService.searchFlashcard("apple", 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/flashcard/search")
                        .param("title", "apple")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Search Successfully"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get flashcard by id")
    void testGetById() throws Exception {
        String id = UUID.randomUUID().toString();

        FlashcardResponse response = new FlashcardResponse();
        response.setId(id);

        when(flashcardService.getFlashcardById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/flashcard/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get Successfully"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("Should create flashcard successfully")
    void testCreateFlashcard() throws Exception {
        String id = UUID.randomUUID().toString();
        String libraryId = UUID.randomUUID().toString();

        FlashcardResponse response = new FlashcardResponse();
        response.setId(id);

        when(flashcardService.createFlashcard(any())).thenReturn(response);

        String json = """
                {
                  "libraryId": "%s",
                  "status": "ACTIVE"
                }
                """.formatted(libraryId);

        mockMvc.perform(post("/api/v1/flashcard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Created Flashcard Successfully"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("Should update flashcard successfully")
    void testUpdateFlashcard() throws Exception {
        String id = UUID.randomUUID().toString();
        String libraryId = UUID.randomUUID().toString();

        FlashcardResponse response = new FlashcardResponse();
        response.setId(id);

        when(flashcardService.updateFlashcard(eq(id), any())).thenReturn(response);

        String json = """
                {
                  "libraryId": "%s",
                  "status": "ACTIVE"
                }
                """.formatted(libraryId);

        mockMvc.perform(put("/api/v1/flashcard/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Update Successfully"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    @DisplayName("Should delete flashcard successfully")
    void testDeleteFlashcard() throws Exception {
        String id = UUID.randomUUID().toString();
        doNothing().when(flashcardService).deleteFlashcard(id);

        mockMvc.perform(delete("/api/v1/flashcard/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Delete Successfully"));
    }
}