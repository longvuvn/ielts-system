package com.ddhva.ielts.dto.library.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LibraryRequest {
    @NotBlank(message = "Name is mandatory")
    private String name;
    private String description;
    private String is_Public;
    @NotBlank(message = "Learner Id is mandatory")
    private String learnerId;
}
