package com.ddhva.ielts.service;

import com.ddhva.ielts.dto.auth.req.AuthLogin;
import com.ddhva.ielts.dto.auth.req.AuthRegister;
import com.ddhva.ielts.dto.auth.req.FirebaseLoginRequest;
import com.ddhva.ielts.dto.auth.res.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthLogin request);
    AuthResponse register(AuthRegister request);
    AuthResponse loginWithGoogle(FirebaseLoginRequest request);
}
