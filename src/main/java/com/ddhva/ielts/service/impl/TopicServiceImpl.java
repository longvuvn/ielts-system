package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.topic.res.TopicResponse;
import com.ddhva.ielts.model.Topic;
import com.ddhva.ielts.repositories.TopicRepository;
import com.ddhva.ielts.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<TopicResponse> getAllTopics() {
        try{
            List<Topic> topics = topicRepository.findAll();
            List<TopicResponse> responses = topics.stream()
                    .map(topic -> modelMapper.map(topic, TopicResponse.class))
                    .collect(Collectors.toList());
            log.info("Successfully mapped {} topics to TopicResponse", responses.size());
            return responses;
        }catch (Exception e){
            log.error("Error while fetching topics from database", e);
            throw new RuntimeException("Error while fetching topics from database");
        }
    }
}
