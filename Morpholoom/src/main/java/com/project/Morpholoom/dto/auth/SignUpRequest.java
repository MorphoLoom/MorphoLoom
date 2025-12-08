package com.project.Morpholoom.dto.auth;

import lombok.Data;

@Data
public class SignUpRequest {
    private String email;
    private String password;
    private String username;
    private String socialProvider;
}

