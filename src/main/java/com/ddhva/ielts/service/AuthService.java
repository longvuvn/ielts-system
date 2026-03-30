package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.auth.req.AuthLogin;
import com.ddhva.ielts.dto.auth.req.AuthRegister;
import com.ddhva.ielts.dto.auth.req.FirebaseLoginRequest;
import com.ddhva.ielts.dto.auth.res.AuthResponse;
import com.ddhva.ielts.dto.refresh.req.RefreshTokenRequest;
import com.ddhva.ielts.dto.refresh.res.RefreshTokenResponse;

public interface AuthService {
    AuthResponse login(AuthLogin request);
    AuthResponse register(AuthRegister request);
    AuthResponse loginWithGoogle(FirebaseLoginRequest request);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
    void logout(RefreshTokenRequest refreshToken);
}