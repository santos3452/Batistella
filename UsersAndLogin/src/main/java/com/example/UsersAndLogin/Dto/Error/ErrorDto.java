package com.example.UsersAndLogin.Dto.Error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDto {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    public static ErrorDto of(int status, String error, String message) {
        return ErrorDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .build();
    }
} 