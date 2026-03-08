package com.ddhva.ielts.topic;


import com.ddhva.ielts.controller.TopicController;
import com.ddhva.ielts.dto.topic.res.TopicResponse;
import com.ddhva.ielts.service.TopicService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TopicController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TopicService topicService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("Should return all topics")
    public void testGetAllTopics() throws Exception {
        TopicResponse response1 = new TopicResponse();
        response1.setId(UUID.randomUUID().toString());
        response1.setName("IELTS - student");
        response1.setStatus("ACTIVE");

        TopicResponse response2 = new TopicResponse();
        response2.setId(UUID.randomUUID().toString());
        response2.setName("IELTS - teacher");
        response2.setStatus("ACTIVE");

        when(topicService.getAllTopics()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/api/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get All Successfully"))
                .andExpect(jsonPath("$.data[0].name").value("IELTS - student"))
                .andExpect(jsonPath("$.data[1].name").value("IELTS - teacher"));
    }
}
