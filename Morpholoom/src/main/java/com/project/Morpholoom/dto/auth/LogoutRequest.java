package com.project.Morpholoom.dto.auth;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
