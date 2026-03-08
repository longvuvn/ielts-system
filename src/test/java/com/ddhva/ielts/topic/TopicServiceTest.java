package com.ddhva.ielts.topic;

import com.ddhva.ielts.dto.topic.res.TopicResponse;
import com.ddhva.ielts.enums.TopicStatus;
import com.ddhva.ielts.model.Topic;
import com.ddhva.ielts.repositories.TopicRepository;
import com.ddhva.ielts.service.impl.TopicServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TopicServiceImpl topicService;

    @Test
    @DisplayName("Should return all topics")
    public void testGetAllTopics() throws NoSuchElementException {
        Topic topic1 = Topic.builder()
                .id(UUID.randomUUID())
                .name("IELTS - student")
                .status(TopicStatus.ACTIVE)
                .build();

        Topic topic2 = Topic.builder()
                .id(UUID.randomUUID())
                .name("IELTS - teacher")
                .status(TopicStatus.ACTIVE)
                .build();

        TopicResponse response1 = new TopicResponse();
        response1.setId(topic1.getId().toString());
        response1.setName("IELTS - student");
        response1.setStatus("ACTIVE");

        TopicResponse response2 = new TopicResponse();
        response2.setId(topic2.getId().toString());
        response2.setName("IELTS - teacher");
        response2.setStatus("ACTIVE");

        when(topicRepository.findAll()).thenReturn(List.of(topic1, topic2));
        when(modelMapper.map(topic1, TopicResponse.class)).thenReturn(response1);
        when(modelMapper.map(topic2, TopicResponse.class)).thenReturn(response2);
        List<TopicResponse> result = topicService.getAllTopics();

        assertEquals(2, result.size());
        assertEquals(topic1.getId().toString(), result.getFirst().getId());
        assertEquals("IELTS - student", result.get(0).getName());
        assertEquals("ACTIVE", result.get(0).getStatus());
        assertEquals(topic2.getId().toString(), result.get(1).getId());
        assertEquals("IELTS - teacher", result.get(1).getName());
        assertEquals("ACTIVE", result.get(1).getStatus());
    }
}