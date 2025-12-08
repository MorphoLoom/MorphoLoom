package com.project.Morpholoom.dto.auth;

import lombok.Data;

@Data
public class SocialLoginRequest {
    private String provider;
    private String token;
}

