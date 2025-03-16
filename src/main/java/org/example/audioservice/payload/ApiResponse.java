package org.example.audioservice.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields automatically
public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;

}
