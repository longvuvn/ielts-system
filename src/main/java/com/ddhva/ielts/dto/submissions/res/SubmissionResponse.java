package com.ddhva.ielts.dto.submissions.res;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponse {
    private String id;
    private String status;
    private String examId;
    private String learnerId;
    private String totalQuestions;
    private String correctAnswer;
    private String failedAnswer;
    private String score;
    private String completed_At;
    private String started_At;
    private String ended_At;
    private List<SubmissionAnswerResponse> submissionAnswers;
}
