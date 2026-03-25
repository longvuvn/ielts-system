package com.ddhva.ielts.model;


import com.ddhva.ielts.enums.ExamStatus;
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
@Table(name = "\"exam\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Exam extends Auditing{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    @Enumerated(EnumType.STRING)
    private ExamStatus status;
    private BigDecimal max_score;
    private Instant duration;
    private String source_url;
    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    private List<Submissions> submissions;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    private List<Section> sections;
}
