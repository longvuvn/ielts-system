package com.ddhva.ielts.dto.learner.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerResponse {
    private String id;
    private String fullName;
    private String email;
    private String username;
    private String phoneNumber;
    private String avatarUrl;
    private String role;
    private String status;
    private String createdAt;
}