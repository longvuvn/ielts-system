package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.TopicStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"topics\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

    @Enumerated(EnumType.STRING)
    private TopicStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "topic")
    private List<Vocabulary> vocabularies;
}
