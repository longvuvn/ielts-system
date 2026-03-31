package com.ddhva.ielts.dto.learner.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearnerUpdateRequest {
    private String fullName;
    private String phoneNumber;
    private String username;
    private String avatarUrl;
}