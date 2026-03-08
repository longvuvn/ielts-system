package com.ddhva.ielts.dto.topic.res;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponse {
    private String id;
    private String name;
    private String status;
}
