package com.ddhva.ielts.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"passages\"")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Passage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String content_html;

    private Integer passage_number;
    @Column(columnDefinition = "TEXT")
    private String instruction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "passage", fetch = FetchType.LAZY)
    private List<Question> questions;
}
