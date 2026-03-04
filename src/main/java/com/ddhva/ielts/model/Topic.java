package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.TopicStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"topics\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
