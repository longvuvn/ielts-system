package com.ddhva.ielts.config;

import com.ddhva.ielts.util.JWTFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/exams").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/exams/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/topics").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/topics/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/section/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/library/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/library/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/flashcard/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/flashcard/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vocabularies/topic/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vocabularies/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/vocabularies/**").permitAll()
                        .requestMatchers(HttpMethod.GET,   "/api/v1/learners/{id}").permitAll()
                        .requestMatchers(HttpMethod.PUT,   "/api/v1/learners/{id}").hasAuthority("LEARNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/learners/{id}/avatar").hasAuthority("LEARNER")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/learners/{id}/history").hasAuthority("LEARNER")
                        .requestMatchers("/api/v1/learners/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/crawler/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}