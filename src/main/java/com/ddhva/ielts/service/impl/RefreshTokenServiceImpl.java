package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.model.RefreshToken;
import com.ddhva.ielts.model.User;
import com.ddhva.ielts.repositories.RefreshTokenRepository;
import com.ddhva.ielts.service.RefreshTokenService;
import com.ddhva.ielts.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${jwt.expiration.refresh-token}")
    private long jwtRefreshToken;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ModelMapper modelMapper;
    private final JWTUtil jwtUtil;


    @Override
    public void createRefreshToken(User user, String refreshTokenRequest) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtRefreshToken, ChronoUnit.MILLIS);
        String token = jwtUtil.generateRefreshToken(user);
        RefreshToken refreshToken = modelMapper.map(refreshTokenRequest, RefreshToken.class);
        refreshToken.setExpiryDate(expiry);
        refreshToken.setUser(user);
        refreshToken.setRefreshToken(token);
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeToken(String refreshToken) {
        Instant now = Instant.now();
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        token.setRevoked(true);
        token.setDeletedAt(now);
        refreshTokenRepository.save(token);
    }

    @Override
    public void validateToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        if(token.getRevoked()){
            throw new RuntimeException("Token is revoked");
        }
        Instant now = Instant.now();
        if(now.isAfter(token.getExpiryDate())){
            throw new RuntimeException("Token is expired");
        }
    }
}
