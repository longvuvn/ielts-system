package com.ddhva.ielts.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "\"sections\"")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String title;

    private Instant time_limit;
    private Integer section_number;
    private String audio_url;
    private String image_url;
    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    private List<Passage> passages;


}
