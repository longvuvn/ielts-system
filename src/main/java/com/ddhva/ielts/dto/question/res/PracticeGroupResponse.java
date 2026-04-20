package com.ddhva.ielts.dto.question.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PracticeGroupResponse {
    private String passageId;
    private String passageHtml;
    private String instruction;
    private String audioUrl;
    private List<PracticeResponse> questions;
}
