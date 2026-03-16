package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"deck_vocabulary\"")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeckVocabulary extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String userDefinition;

    private Integer reviewCount = 0;

    @Enumerated(EnumType.STRING)
    private ReviewStatus reviewStatus;

    private Instant lastReviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id")
    private Vocabulary vocabulary;
}
