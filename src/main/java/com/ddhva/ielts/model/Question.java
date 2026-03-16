package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.LevelType;
import com.ddhva.ielts.enums.QuestionStatus;
import com.ddhva.ielts.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"questions\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Question extends Auditing{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String image_url;
    private String audio_url;
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    private QuestionStatus status;

    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    private LevelType level;
    private String notes;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<Answer> answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;



    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY)
    private List<SubmissionAnswer> submissionAnswers;
}
