package com.ddhva.ielts.controller;

import com.ddhva.ielts.service.exception.ApiResponse;
import com.ddhva.ielts.dto.topic.res.TopicResponse;
import com.ddhva.ielts.service.TopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAll() {
        List<TopicResponse> topics = topicService.getAllTopics();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Get All Successfully",
                        topics
                )
        );
    }
}
