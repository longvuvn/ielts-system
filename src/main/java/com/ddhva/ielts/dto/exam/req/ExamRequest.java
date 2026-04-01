package com.ddhva.ielts.dto.exam.req;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamRequest {
    private String title;
    private String status;
    private String max_score;
    private String duration;
}
