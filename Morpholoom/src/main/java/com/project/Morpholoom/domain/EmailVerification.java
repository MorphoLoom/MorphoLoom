package com.project.Morpholoom.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EmailVerification {
    private Long id;
    private String email;
    private String verificationCode;
    private LocalDateTime expiresAt;
    private boolean verified;
    private LocalDateTime createdAt;
}
