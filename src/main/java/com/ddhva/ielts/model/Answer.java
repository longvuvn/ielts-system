package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.AnswerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "\"answers\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Answer extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String content;

    @Enumerated(EnumType.STRING)
    private AnswerStatus status;

    @Column(columnDefinition = "TEXT")
    private Boolean is_correct;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}
