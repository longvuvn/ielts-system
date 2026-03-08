package com.ddhva.ielts.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "\"submission_answer\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Boolean is_correct;
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    @Lob
    private String answerOption;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submissions submissions;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}
