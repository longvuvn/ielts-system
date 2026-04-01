package com.ddhva.ielts.service;
import com.ddhva.ielts.dto.submissions.req.SubmissionRequest;
import com.ddhva.ielts.dto.submissions.res.SubmissionResponse;



public interface SubmissionService {
    SubmissionResponse createSubmission(SubmissionRequest submissionRequest);
}
