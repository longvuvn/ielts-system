package com.ddhva.ielts.controller;

import com.ddhva.ielts.dto.auth.req.AuthLogin;
import com.ddhva.ielts.dto.auth.req.AuthRegister;
import com.ddhva.ielts.dto.auth.req.FirebaseLoginRequest;
import com.ddhva.ielts.dto.auth.res.AuthResponse;
import com.ddhva.ielts.service.AuthService;
import com.ddhva.ielts.service.exception.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthLogin request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Login Successfully", response)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody AuthRegister request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(), "Register Successfully", response)
        );
    }

    @PostMapping("/login/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody FirebaseLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Google Login Successfully", response)
        );
    }
}