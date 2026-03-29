package com.ddhva.ielts.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.access-token}")
    private long jwtAccessToken;

    @Value("${jwt.expiration.refresh-token}")
    private long jwtRefreshToken;

    public long getJwtRefreshToken() {
        return jwtRefreshToken;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ✅ GENERATE ACCESS TOKEN
    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtAccessToken))
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ GENERATE REFRESH TOKEN
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtRefreshToken))
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ EXTRACT EMAIL
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // ✅ VALIDATE TOKEN
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())   // ✅ dùng cái này
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}