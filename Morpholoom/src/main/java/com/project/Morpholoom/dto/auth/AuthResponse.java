package com.project.Morpholoom.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
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

