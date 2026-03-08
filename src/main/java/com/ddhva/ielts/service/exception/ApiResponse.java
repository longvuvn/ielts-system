package com.ddhva.ielts.service.exception;



import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T>{
    public int status;
    public String message;
    public T data;

    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
