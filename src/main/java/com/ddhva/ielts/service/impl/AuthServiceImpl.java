package com.ddhva.ielts.service.impl;

import com.ddhva.ielts.dto.auth.req.AuthLogin;
import com.ddhva.ielts.dto.auth.req.AuthRegister;
import com.ddhva.ielts.dto.auth.req.FirebaseLoginRequest;
import com.ddhva.ielts.dto.auth.res.AuthResponse;
import com.ddhva.ielts.enums.RoleStatus;
import com.ddhva.ielts.enums.UserStatus;
import com.ddhva.ielts.model.Learner;
import com.ddhva.ielts.model.RefreshToken;
import com.ddhva.ielts.model.Role;
import com.ddhva.ielts.repositories.LearnerRepository;
import com.ddhva.ielts.repositories.RefreshTokenRepository;
import com.ddhva.ielts.repositories.RoleRepository;
import com.ddhva.ielts.repositories.UserRepository;
import com.ddhva.ielts.service.AuthService;
import com.ddhva.ielts.util.JWTUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LearnerRepository learnerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public AuthResponse login(AuthLogin request) {
        Learner learner = learnerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        if (!passwordEncoder.matches(request.getPassword(), learner.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        log.info("[AUTH] Login success: {}", learner.getEmail());
        return buildAuthResponse(learner);
    }

    @Override
    @Transactional
    public AuthResponse register(AuthRegister request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        Role role = getOrCreateLearnerRole();

        Learner learner = new Learner();
        learner.setFullName(request.getFullName());
        learner.setEmail(request.getEmail());
        learner.setUsername(request.getUsername());
        learner.setPassword(passwordEncoder.encode(request.getPassword()));
        learner.setStatus(UserStatus.ACTIVE);
        learner.setRole(role);
        learnerRepository.save(learner);

        log.info("[AUTH] Registered new learner: {}", learner.getEmail());
        return buildAuthResponse(learner);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(FirebaseLoginRequest request) {
        try {
            // 1. Verify Firebase token
            FirebaseToken firebaseToken = FirebaseAuth.getInstance()
                    .verifyIdToken(request.getIdToken());

            String email    = firebaseToken.getEmail();
            String fullName = firebaseToken.getName();

            log.info("[AUTH] Google login: {}", email);

            // 2. Tìm user trong DB, nếu chưa có thì tạo mới với role LEARNER
            Optional<Learner> existing = learnerRepository.findByEmail(email);
            Learner learner = existing.orElseGet(() -> createLearnerFromGoogle(email, fullName));

            return buildAuthResponse(learner);

        } catch (Exception e) {
            log.error("[AUTH] Google login failed: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid Firebase token");
        }
    }



    private Learner createLearnerFromGoogle(String email, String fullName) {
        Role role = getOrCreateLearnerRole();

        Learner learner = new Learner();
        learner.setFullName(fullName != null ? fullName : email);
        learner.setEmail(email);
        learner.setUsername(email);
        learner.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        learner.setStatus(UserStatus.ACTIVE);
        learner.setRole(role);
        learnerRepository.save(learner);

        log.info("[AUTH] Created new learner from Google: {}", email);
        return learner;
    }

    private Role getOrCreateLearnerRole() {
        return roleRepository.findByName("LEARNER").orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName("LEARNER");
            newRole.setStatus(RoleStatus.ACTIVE);
            return roleRepository.save(newRole);
        });
    }

    private AuthResponse buildAuthResponse(Learner learner) {
        String accessToken  = jwtUtil.generateAccessToken(learner);
        String refreshToken = jwtUtil.generateRefreshToken(learner.getEmail());

        RefreshToken token = new RefreshToken();
        token.setRefreshToken(refreshToken);
        token.setUser(learner);
        token.setRevoked(false);
        token.setExpiryDate(Instant.now().plusMillis(jwtUtil.getJwtRefreshToken()));
        refreshTokenRepository.save(token);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .fullName(learner.getFullName())
                .email(learner.getEmail())
                .role(learner.getRole() != null ? learner.getRole().getName() : "LEARNER")
                .build();
    }
}