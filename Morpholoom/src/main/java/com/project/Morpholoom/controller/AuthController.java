package com.project.Morpholoom.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Morpholoom.dto.auth.AuthResponse;
import com.project.Morpholoom.dto.auth.DeleteAccountRequest;
import com.project.Morpholoom.dto.auth.DeleteAccountResponse;
import com.project.Morpholoom.dto.auth.EmailVerificationRequest;
import com.project.Morpholoom.dto.auth.EmailVerificationResponse;
import com.project.Morpholoom.dto.auth.LoginRequest;
import com.project.Morpholoom.dto.auth.LogoutRequest;
import com.project.Morpholoom.dto.auth.PasswordResetResponse;
import com.project.Morpholoom.dto.auth.PasswordResetVerifyRequest;
import com.project.Morpholoom.dto.auth.RefreshTokenRequest;
import com.project.Morpholoom.dto.auth.SendVerificationRequest;
import com.project.Morpholoom.dto.auth.SignUpRequest;
import com.project.Morpholoom.dto.auth.SocialLoginRequest;
import com.project.Morpholoom.service.AuthService;
import com.project.Morpholoom.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원 가입, 로그인, 소셜 로그인, 이메일 인증, 토큰 관리, 비밀번호 재설정, 계정 삭제")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/send-verification")
    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입 전 이메일 인증을 위한 코드를 발송합니다.")
    public ResponseEntity<EmailVerificationResponse> sendVerificationCode(@RequestBody SendVerificationRequest request) {
        return ResponseEntity.ok(emailService.sendVerificationCode(request.getEmail()));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "이메일 인증 코드 확인", description = "발송된 인증 코드를 확인하여 이메일 인증을 완료합니다.")
    public ResponseEntity<EmailVerificationResponse> verifyEmail(@RequestBody EmailVerificationRequest request) {
        return ResponseEntity.ok(emailService.verifyCode(request.getEmail(), request.getVerificationCode()));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "이메일 인증 완료 후, 이메일/비밀번호/닉네임으로 회원 가입을 처리합니다. Keycloak에 사용자가 생성되고 토큰이 발급됩니다.")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Keycloak에서 액세스/리프레시 토큰을 발급받습니다.")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받습니다.")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃합니다.")
    public ResponseEntity<Map<String, String>> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    @PostMapping("/social-login")
    @Operation(summary = "소셜 로그인", description = "소셜 provider/token 기반으로 신규 가입 또는 로그인을 수행합니다.")
    public ResponseEntity<AuthResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }

    @PostMapping("/delete-account")
    @Operation(summary = "계정 삭제", description = "이메일과 비밀번호를 확인하여 Keycloak 및 로컬 DB에서 계정을 삭제합니다.")
    public ResponseEntity<DeleteAccountResponse> deleteAccount(@RequestBody DeleteAccountRequest request) {
        return ResponseEntity.ok(authService.deleteAccount(request));
    }

    @PostMapping("/password-reset/verify")
    @Operation(summary = "비밀번호 재설정", description = "인증 코드를 확인하고 새로운 비밀번호를 설정합니다.")
    public ResponseEntity<PasswordResetResponse> resetPassword(@RequestBody PasswordResetVerifyRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}

