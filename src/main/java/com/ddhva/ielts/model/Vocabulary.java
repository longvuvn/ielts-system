package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.VocabularyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.sql.ConnectionBuilder;
import java.util.UUID;

@Entity
@Table(name = "\"vocabulary\"")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vocabulary extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String word;
    private String ipa;
    private String definition;
    private String example;
    private String audio_url;
    private String part_of_speech;

    @Enumerated(EnumType.STRING)
    private VocabularyStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id")
    private Flashcard flashcard;
}
