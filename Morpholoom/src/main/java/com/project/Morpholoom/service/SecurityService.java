package com.project.Morpholoom.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.project.Morpholoom.domain.User;
import com.project.Morpholoom.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final UserMapper userMapper;

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 현재 인증된 사용자 정보를 반환합니다.
     */
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            return null;
        }
        return userMapper.findByEmail(email);
    }

    /**
     * JWT에서 현재 사용자의 이메일을 추출합니다.
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 정보가 없습니다.");
            return null;
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Jwt jwt) {
            // Keycloak JWT에서 이메일 추출 (preferred_username 또는 email 클레임)
            String email = jwt.getClaimAsString("email");
            if (email == null) {
                email = jwt.getClaimAsString("preferred_username");
            }
            if (email == null) {
                email = jwt.getSubject();
            }
            return email;
        }
        
        return null;
    }

    /**
     * JWT 토큰 자체를 반환합니다.
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        
        return null;
    }
}
