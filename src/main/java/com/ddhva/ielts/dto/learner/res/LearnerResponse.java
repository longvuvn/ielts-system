package com.ddhva.ielts.dto.learner.res;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LearnerResponse {

    private String id;
    private String fullName;
    private String email;
    private String role;
}