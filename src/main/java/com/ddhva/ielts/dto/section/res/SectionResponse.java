package com.ddhva.ielts.dto.section.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionResponse {
    private String id;
    private String title;
    private String time_limit;
    private String section_number;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String audio_url;
}
