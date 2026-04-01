package com.ddhva.ielts.dto.auth.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRegister {
    @NotBlank(message = "Full name is mandatory")
    private String fullName;
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotBlank(message = "Username is mandatory")
    private String username;
    @NotBlank(message = "Password is mandatory")
    private String password;
}