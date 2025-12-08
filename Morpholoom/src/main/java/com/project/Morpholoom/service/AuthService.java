package com.project.Morpholoom.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.project.Morpholoom.domain.User;
import com.project.Morpholoom.dto.auth.AuthResponse;
import com.project.Morpholoom.dto.auth.LoginRequest;
import com.project.Morpholoom.dto.auth.SignUpRequest;
import com.project.Morpholoom.dto.auth.SocialLoginRequest;
import com.project.Morpholoom.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse signUp(SignUpRequest request) {
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setSocialProvider(StringUtils.hasText(request.getSocialProvider()) ? request.getSocialProvider() : "NONE");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insertUser(user);

        return buildAuthResponse(user, true, true);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return buildAuthResponse(user, true, false);
    }

    public AuthResponse socialLogin(SocialLoginRequest request) {
        User user = userMapper.findByEmail(request.getProvider() + ":" + request.getToken());
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setEmail(request.getProvider() + ":" + request.getToken());
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setUsername("user_" + UUID.randomUUID().toString().substring(0, 8));
            user.setSocialProvider(request.getProvider());
            user.setCreatedAt(LocalDateTime.now());
            userMapper.insertUser(user);
            isNew = true;
        }
        return buildAuthResponse(user, true, isNew);
    }

    private AuthResponse buildAuthResponse(User user, boolean includeUser, boolean isNew) {
        String access = "jwt-" + UUID.randomUUID();
        String refresh = "jwt-refresh-" + UUID.randomUUID();

        AuthResponse.UserSummary summary = includeUser
                ? new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getUsername())
                : null;

        return new AuthResponse(access, refresh, summary, isNew);
    }
}

