package com.project.Morpholoom.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserSummary user;
    private Boolean isNewUser;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserSummary {
        private Long userId;
        private String email;
        private String username;
    }
}

