package com.ddhva.ielts.dto.question.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PracticeTestResponse {
    private String id;
    private int totalQuestions; // Total number of individual questions in this response
    private long totalWrongQuestions; // Total unique wrong questions for the learner
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private List<PracticeGroupResponse> groups; // Questions grouped by passage/section context
}
