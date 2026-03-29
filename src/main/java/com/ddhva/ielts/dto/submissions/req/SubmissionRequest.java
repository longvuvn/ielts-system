package com.ddhva.ielts.dto.submissions.req;


import com.ddhva.ielts.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionRequest {
    private String learnerId;
    private String examId;
    public List<SubmissionAnswerRequest> submissionAnswerRequests;
}
