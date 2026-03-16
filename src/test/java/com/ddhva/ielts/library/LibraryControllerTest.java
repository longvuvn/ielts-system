package com.ddhva.ielts.library;

import com.ddhva.ielts.controller.LibraryController;
import com.ddhva.ielts.dto.library.req.LibraryRequest;
import com.ddhva.ielts.dto.library.res.LibraryResponse;
import com.ddhva.ielts.dto.pagination.Pagination;
import com.ddhva.ielts.service.LibraryService;
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

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc(addFilters = false)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService libraryService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Should return libraries by learner id")
    void testGetAllByLearnerId() throws Exception {
        String learnerId = UUID.randomUUID().toString();

        LibraryResponse item = new LibraryResponse();
        item.setId(UUID.randomUUID().toString());
        item.setName("English Book");
        item.setLearnerId(learnerId);

        Pagination<LibraryResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(item));

        when(libraryService.getAllLibrariesByLearnerId(learnerId, 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/library/learner/{learnerId}", learnerId)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Libraries fetched successfully"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("English Book"))
                .andExpect(jsonPath("$.data.content[0].learnerId").value(learnerId));
    }

    @Test
    @DisplayName("Should search libraries successfully")
    void testSearchLibrary() throws Exception {
        String name = "English";

        LibraryResponse item = new LibraryResponse();
        item.setId(UUID.randomUUID().toString());
        item.setName("English Grammar");

        Pagination<LibraryResponse> pagination = new Pagination<>();
        pagination.setPage(0);
        pagination.setSize(10);
        pagination.setTotalPages(1);
        pagination.setTotalElements(1);
        pagination.setContent(List.of(item));

        when(libraryService.searchLibrary(name, 0, 10)).thenReturn(pagination);

        mockMvc.perform(get("/api/v1/library/search")
                        .param("name", name)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Libraries fetched successfully"))
                .andExpect(jsonPath("$.data.content[0].name").value("English Grammar"));
    }

    @Test
    @DisplayName("Should return library by id")
    void testGetById() throws Exception {
        String id = UUID.randomUUID().toString();

        LibraryResponse response = new LibraryResponse();
        response.setId(id);
        response.setName("Library A");
        response.setLearnerId(UUID.randomUUID().toString());

        when(libraryService.getLibraryById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Library fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("Library A"));
    }

    @Test
    @DisplayName("Should create library successfully")
    void testCreateLibrary() throws Exception {
        String learnerId = UUID.randomUUID().toString();

        LibraryRequest request = new LibraryRequest();
        request.setName("New Library");
        request.setLearnerId(learnerId);

        LibraryResponse response = new LibraryResponse();
        response.setId(UUID.randomUUID().toString());
        response.setName("New Library");
        response.setLearnerId(learnerId);

        when(libraryService.createLibrary(any(LibraryRequest.class))).thenReturn(response);
        String json = """
                {
                  "name": "New Library",
                  "learnerId": "%s"
                }
                """.formatted(learnerId);
        mockMvc.perform(post("/api/v1/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Library created successfully"))
                .andExpect(jsonPath("$.data.name").value("New Library"));
    }

    @Test
    @DisplayName("Should update library successfully")
    void testUpdateLibrary() throws Exception {
        String id = UUID.randomUUID().toString();

        LibraryRequest request = new LibraryRequest();
        request.setName("Updated Library");

        LibraryResponse response = new LibraryResponse();
        response.setId(id);
        response.setName("Updated Library");

        when(libraryService.updateLibrary(eq(id), any(LibraryRequest.class))).thenReturn(response);
        String json = """
                {
                  "name": "Updated Library"
                }
                """;
        mockMvc.perform(put("/api/v1/library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Library updated successfully"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("Updated Library"));
    }

    @Test
    @DisplayName("Should soft delete library successfully")
    void testSoftDeleteLibrary() throws Exception {
        String id = UUID.randomUUID().toString();
        doNothing().when(libraryService).softDeleteLibrary(id);

        mockMvc.perform(delete("/api/v1/library/soft-delete/{libraryId}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Library soft deleted successfully"));
    }

    @Test
    @DisplayName("Should delete library successfully")
    void testDeleteLibrary() throws Exception {
        String id = UUID.randomUUID().toString();
        doNothing().when(libraryService).deleteLibrary(id);

        mockMvc.perform(delete("/api/v1/library/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Library deleted successfully"));
    }
}