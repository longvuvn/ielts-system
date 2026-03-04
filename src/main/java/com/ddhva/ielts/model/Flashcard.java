package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.FlashcardStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"flashcards\"")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Flashcard extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;

    @Enumerated(EnumType.STRING)
    private FlashcardStatus status;

    @ManyToOne
    @JoinColumn(name = "library_id")
    private Library library;

    @OneToMany(mappedBy = "flashcard", fetch = FetchType.LAZY)
    private List<Vocabulary> vocabularies;
}
