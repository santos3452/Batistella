package com.example.Products.Dtos.Error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private Long timestamp;
    private int status;
    private String error;
    private String message;

    public static ErrorDto of(int status, String error, String message) {
        return ErrorDto.builder()
                .timestamp(System.currentTimeMillis())
                .status(status)
                .error(error)
                .message(message)
                .build();
    }
}