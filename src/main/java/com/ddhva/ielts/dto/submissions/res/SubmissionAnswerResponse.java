package com.ddhva.ielts.dto.submissions.res;

import com.ddhva.ielts.dto.writing.WritingFeedbackResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubmissionAnswerResponse {
    private String id;
    private String submission_id;
    private String question_id;
    private String is_correct;
    private String answerText;
    private String answerOption;
    private WritingFeedbackResponse writingFeedback;
}