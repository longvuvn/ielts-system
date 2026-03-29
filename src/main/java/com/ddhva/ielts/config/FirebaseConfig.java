package com.ddhva.ielts.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {

                String firebasePath = System.getenv("FIREBASE_KEY_PATH");

                GoogleCredentials credentials = GoogleCredentials
                        .fromStream(new java.io.FileInputStream(firebasePath));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("[FIREBASE] Initialized successfully");
            }
        } catch (IOException e) {
            log.error("[FIREBASE] Error initializing: {}", e.getMessage());
        }
    }
}