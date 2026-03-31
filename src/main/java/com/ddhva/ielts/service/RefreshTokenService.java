package com.ddhva.ielts.service;

import com.ddhva.ielts.model.User;

public interface RefreshTokenService {
    void createRefreshToken(User user, String refreshTokenRequest);
    void revokeToken (String refreshToken);
    void validateToken(String refreshToken);
}
