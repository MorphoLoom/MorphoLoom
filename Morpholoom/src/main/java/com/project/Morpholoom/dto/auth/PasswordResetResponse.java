package com.project.Morpholoom.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetResponse {
    private boolean success;
    private String message;

    public static PasswordResetResponse success(String message) {
        return PasswordResetResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static PasswordResetResponse failure(String message) {
        return PasswordResetResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
