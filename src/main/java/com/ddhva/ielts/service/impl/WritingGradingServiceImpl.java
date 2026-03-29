package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.writing.req.WritingRequest;
import com.ddhva.ielts.dto.writing.res.WritingFeedbackResponse;
import com.ddhva.ielts.service.WritingGradingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
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

    @Value("${Gemini.api.key}")
    private String apiKey;

    @Value("${Gemini.api.url}")
    private String apiUrl;

    @Override
    public WritingFeedbackResponse grade(WritingRequest writingRequest) {
        try {
            log.info("[WRITING-GRADING] Calling Gemini...");

            String prompt = buildPrompt(
                    writingRequest.getQuestionId(),
                    writingRequest.getAnswerText()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String url = apiUrl + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseGeminiResponse(response.getBody());
            }

            log.error("[GEMINI ERROR] Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            return error("Gemini error: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("[WRITING-GRADING] ERROR", e);
            return error(e.getMessage());
        }
    }

    private WritingFeedbackResponse parseGeminiResponse(String body) throws Exception {

        JsonNode root = objectMapper.readTree(body);

        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return error("No candidates from Gemini");
        }

        JsonNode parts = candidates.get(0)
                .path("content")
                .path("parts");

        if (!parts.isArray() || parts.isEmpty()) {
            return error("No content parts from Gemini");
        }

        String content = parts.get(0).path("text").asText();

        if (content == null || content.isBlank()) {
            return error("Empty Gemini response");
        }

        // 🔥 FIX QUAN TRỌNG: clean markdown
        content = cleanJson(content);

        JsonNode node;
        try {
            node = objectMapper.readTree(content);
        } catch (Exception e) {
            // log để debug
            log.error("Invalid JSON from Gemini: {}", content);
            return error("Gemini returned invalid JSON format");
        }

        return WritingFeedbackResponse.builder()
                .band(parseBand(node))
                .taskAchievement(node.path("taskAchievement").asText(""))
                .coherenceCohesion(node.path("coherenceCohesion").asText(""))
                .lexicalResource(node.path("lexicalResource").asText(""))
                .grammaticalRange(node.path("grammaticalRange").asText(""))
                .overallFeedback(node.path("overallFeedback").asText(""))
                .correctedEssay(node.path("correctedEssay").asText(""))
                .build();
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

    private BigDecimal parseBand(JsonNode node) {
        try {
            BigDecimal band = new BigDecimal(node.path("band").asText("0"));
            return band.min(BigDecimal.valueOf(9)).max(BigDecimal.ZERO);
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

    private String cleanJson(String raw) {
        if (raw == null) return null;

        return raw
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();
    }
}