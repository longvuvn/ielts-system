package com.ddhva.ielts.dto.deckvocabulary.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    private String isCorrect;
}
