package com.ddhva.ielts.model;


import com.ddhva.ielts.service.crawler.BooleanToStringConverter;
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

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "is_correct", columnDefinition = "VARCHAR(5)")
    private Boolean isCorrect;

    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String answerText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String answerOption;

    @Column(columnDefinition = "TEXT")
    private String writingFeedback;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submissions submissions;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;
}
