package com.ddhva.ielts.dto.auth.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseLoginRequest {
    @NotBlank(message = "Firebase token is mandatory")
    private String idToken;
}