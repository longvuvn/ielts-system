package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.VocabularySource;
import com.ddhva.ielts.enums.VocabularyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.sql.ConnectionBuilder;
import java.util.Dictionary;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    private VocabularySource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @OneToMany(mappedBy = "vocabulary", fetch = FetchType.LAZY)
    private List<DeckVocabulary> deckVocabularies;
}
