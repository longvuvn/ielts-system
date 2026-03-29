package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.writing.WritingFeedbackResponse;
import com.ddhva.ielts.service.WritingGradingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WritingGradingServiceImpl implements WritingGradingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";

    @Override
    public WritingFeedbackResponse grade(String taskQuestion, String essayAnswer) {
        try {
            log.info("[WRITING-GRADING] Calling OpenAI...");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "text", Map.of(
                            "format", Map.of("type", "json_object")
                    ),
                    "input", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", List.of(
                                            Map.of(
                                                    "type", "input_text", // 🔥 FIX Ở ĐÂY
                                                    "text", buildPrompt(taskQuestion, essayAnswer)
                                            )
                                    )
                            )
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENAI_URL,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseResponse(response.getBody());
            }

            return error("OpenAI error: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("[WRITING-GRADING] ERROR", e);
            return error(e.getMessage());
        }
    }

    private String buildPrompt(String task, String essay) {
        return """
        You are a strict IELTS Writing examiner.

        Score the essay based on IELTS official band descriptors.

        Be objective and critical.

        Return ONLY valid JSON.

        FORMAT:
        {
          "band": 6.5,
          "taskAchievement": "...",
          "coherenceCohesion": "...",
          "lexicalResource": "...",
          "grammaticalRange": "...",
          "overallFeedback": "...",
          "correctedEssay": "..."
        }

        TASK:
        %s

        ESSAY:
        %s
        """.formatted(task, essay);
    }

    private WritingFeedbackResponse parseResponse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        JsonNode output = root.path("output");
        if (!output.isArray() || output.isEmpty()) {
            return error("Invalid OpenAI response");
        }

        String content = output.get(0)
                .path("content").get(0)
                .path("text")
                .asText();

        JsonNode node = objectMapper.readTree(content);

        return WritingFeedbackResponse.builder()
                .band(parseBand(node))
                .taskAchievement(node.path("taskAchievement").asText())
                .coherenceCohesion(node.path("coherenceCohesion").asText())
                .lexicalResource(node.path("lexicalResource").asText())
                .grammaticalRange(node.path("grammaticalRange").asText())
                .overallFeedback(node.path("overallFeedback").asText())
                .correctedEssay(node.path("correctedEssay").asText())
                .build();
    }

    private BigDecimal parseBand(JsonNode node) {
        try {
            return new BigDecimal(node.path("band").asText("0"));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private WritingFeedbackResponse error(String msg) {
        return WritingFeedbackResponse.builder()
                .band(BigDecimal.ZERO)
                .overallFeedback("ERROR: " + msg)
                .build();
    }
}