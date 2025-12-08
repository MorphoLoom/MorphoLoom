package com.project.Morpholoom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.Morpholoom.dto.auth.AuthResponse;
import com.project.Morpholoom.dto.auth.LoginRequest;
import com.project.Morpholoom.dto.auth.SignUpRequest;
import com.project.Morpholoom.dto.auth.SocialLoginRequest;
import com.project.Morpholoom.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원 가입, 로그인, 소셜 로그인")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "이메일/비밀번호/닉네임으로 회원 가입을 처리합니다.")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 액세스/리프레시 토큰을 발급합니다.")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/social-login")
    @Operation(summary = "소셜 로그인", description = "소셜 provider/token 기반으로 신규 가입 또는 로그인을 수행합니다.")
    public ResponseEntity<AuthResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }
}

