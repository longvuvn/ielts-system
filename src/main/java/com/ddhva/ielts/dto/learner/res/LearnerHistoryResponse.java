package com.ddhva.ielts.dto.learner.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerHistoryResponse {
    private String learnerId;
    private String fullName;
    private String email;
    private Integer totalExamsTaken;
    private String averageScore;
    private List<SubmissionHistoryDto> submissions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubmissionHistoryDto {
        private String submissionId;
        private String examTitle;
        private String score;
        private String totalQuestions;
        private String correctQuestions;
        private String failedQuestions;
        private String status;
        private String completedAt;
        private String startTime;
        private String endTime;
    }
}