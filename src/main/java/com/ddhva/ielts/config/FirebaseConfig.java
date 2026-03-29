package com.ddhva.ielts.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;



@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.service-account}")
    private String firebaseAccount;

    @PostConstruct
    public void initFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                File file = new File(firebaseAccount);
                if (!file.exists()) {
                    throw new RuntimeException(
                            "Firebase key file not found: " + firebaseAccount
                    );
                }
                GoogleCredentials credentials =
                        GoogleCredentials.fromStream(new FileInputStream(file));
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("[FIREBASE] Initialized successfully");
            }
        } catch (Exception e) {
            log.error("[FIREBASE] Error initializing: {}", e.getMessage());
        }
    }
}