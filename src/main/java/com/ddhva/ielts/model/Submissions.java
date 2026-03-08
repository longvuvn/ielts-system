package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Submissions extends Auditing{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private BigDecimal score;
    private Integer correctQuestions;
    private Integer failedQuestions;
    private Integer totalQuestions;
    private Instant startTime;
    private Instant endTime;
    private Instant completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id")
    private Learner learner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @OneToMany(mappedBy = "submissions", fetch = FetchType.LAZY)
    private List<SubmissionAnswer> submissionAnswers;
}
