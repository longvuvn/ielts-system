package com.ddhva.ielts.dto.exam.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrawledExamDto {
    private String title;
    private List<CrawledSectionDto> sections;

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CrawledSectionDto {
        private String title;
        private String skillType;
        private Integer sectionNumber;
        private List<CrawledQuestionDto> questions;
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CrawledQuestionDto {
        private String content;
        private String questionType;
        private List<CrawledAnswerDto> answers;
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class CrawledAnswerDto {
        private String content;
        private Boolean isCorrect;
    }
}