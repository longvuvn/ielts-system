package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.topic.res.TopicResponse;

import java.util.List;

public interface TopicService {
    List<TopicResponse> getAllTopics();
}
