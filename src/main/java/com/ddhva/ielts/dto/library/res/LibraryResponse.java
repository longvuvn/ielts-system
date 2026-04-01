package com.ddhva.ielts.dto.library.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryResponse {
    private String id;
    private String name;
    private String description;
    private String is_Public;
    private String learnerId;
    private String status;
    private String createdAt;
    private String updatedAt;
}
