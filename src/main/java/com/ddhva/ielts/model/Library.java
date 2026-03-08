package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.LibraryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"libraries\"")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Library extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String name;

    private Boolean is_Public;
    private String description;

    @Enumerated(EnumType.STRING)
    private LibraryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private Learner learner;

    @OneToMany(mappedBy = "library", fetch = FetchType.LAZY)
    private List<Flashcard> flashcards;
}
