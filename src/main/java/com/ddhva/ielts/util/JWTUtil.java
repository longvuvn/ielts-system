package com.ddhva.ielts.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.access-token}")
    private long jwtAccessToken;

    @Value("${jwt.expiration.refresh-token}")
    private long jwtRefreshToken;
}
