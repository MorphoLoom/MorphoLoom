package com.project.Morpholoom.dto.auth;

import lombok.Data;

@Data
public class PasswordResetVerifyRequest {
    private String email;
    // private String verificationCode;
    private String newPassword;
}
