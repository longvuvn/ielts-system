package com.ddhva.ielts.dto.section.res;


import com.ddhva.ielts.dto.passage.PassageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionQuestion {
    private String id;
    private String title;
    private String time_limit;
    private String section_number;
    private String audio_url;
    private String image_url;
    List<PassageResponse> passageResponses;
}
