package com.ddhva.ielts.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"refresh_tokens\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken extends Auditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    private Instant expiryDate;
    private Boolean revoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}