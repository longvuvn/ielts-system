package com.ddhva.ielts.dto.exam.res;


import com.ddhva.ielts.dto.section.res.SectionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamResponse {
    private String id;
    private String title;
    private String status;
    private String max_score;
    private String duration;
    private String skillType;
    private String createdAt;
    private String updatedAt;
}
