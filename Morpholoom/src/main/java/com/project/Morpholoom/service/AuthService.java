package com.project.Morpholoom.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.project.Morpholoom.domain.User;
import com.project.Morpholoom.dto.auth.AuthResponse;
import com.project.Morpholoom.dto.auth.DeleteAccountRequest;
import com.project.Morpholoom.dto.auth.DeleteAccountResponse;
import com.project.Morpholoom.dto.auth.KeycloakTokenResponse;
import com.project.Morpholoom.dto.auth.LoginRequest;
import com.project.Morpholoom.dto.auth.PasswordResetResponse;
import com.project.Morpholoom.dto.auth.PasswordResetVerifyRequest;
import com.project.Morpholoom.dto.auth.SignUpRequest;
import com.project.Morpholoom.dto.auth.SocialLoginRequest;
import com.project.Morpholoom.mapper.EmailVerificationMapper;
import com.project.Morpholoom.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final EmailVerificationMapper emailVerificationMapper;
    private final KeycloakService keycloakService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 계정을 삭제합니다.
     */
    public DeleteAccountResponse deleteAccount(DeleteAccountRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        
        if (user == null) {
            return DeleteAccountResponse.failure("존재하지 않는 계정입니다.");
        }
        
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return DeleteAccountResponse.failure("비밀번호가 일치하지 않습니다.");
        }
        
        // Keycloak에서 사용자 삭제
        try {
            keycloakService.deleteUser(request.getEmail());
        } catch (Exception e) {
            log.warn("Keycloak 사용자 삭제 실패 (이미 삭제되었을 수 있음): {}", e.getMessage());
        }
        
        // 이메일 인증 정보 삭제
        emailVerificationMapper.deleteByEmail(request.getEmail());
        
        // 사용자 삭제
        userMapper.deleteByEmail(request.getEmail());
        
        return DeleteAccountResponse.success("계정이 성공적으로 삭제되었습니다.");
    }

    /**
     * 회원가입: Keycloak에 사용자 생성 후 토큰 발급
     */
    public AuthResponse signUp(SignUpRequest request) {
        // 이메일 인증 여부 확인
        if (!emailService.isEmailVerified(request.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 필요합니다. 먼저 이메일 인증을 완료해주세요.");
        }
        
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // Keycloak에 사용자 생성
        keycloakService.createUser(request.getEmail(), request.getPassword(), request.getUsername());

        // 로컬 DB에 사용자 정보 저장
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setSocialProvider(StringUtils.hasText(request.getSocialProvider()) ? request.getSocialProvider() : "NONE");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insertUser(user);

        // Keycloak에서 토큰 발급
        KeycloakTokenResponse tokenResponse = keycloakService.login(request.getEmail(), request.getPassword());

        return buildAuthResponse(user, tokenResponse, true);
    }

    /**
     * 로그인: Keycloak에서 토큰 발급
     */
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // Keycloak에서 토큰 발급 (비밀번호 검증도 Keycloak에서 수행)
        KeycloakTokenResponse tokenResponse = keycloakService.login(request.getEmail(), request.getPassword());

        return buildAuthResponse(user, tokenResponse, false);
    }

    /**
     * 토큰 갱신
     */
    public AuthResponse refreshToken(String refreshToken) {
        KeycloakTokenResponse tokenResponse = keycloakService.refreshToken(refreshToken);
        
        return AuthResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .isNewUser(false)
                .build();
    }

    /**
     * 로그아웃
     */
    public void logout(String refreshToken) {
        keycloakService.logout(refreshToken);
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
        
        // 소셜 로그인의 경우 별도의 토큰 발급 로직 필요 (Keycloak Identity Provider 설정 필요)
        // 현재는 임시 토큰 반환
        return AuthResponse.builder()
                .accessToken("social-login-token")
                .refreshToken("social-refresh-token")
                .user(new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getUsername()))
                .isNewUser(isNew)
                .build();
    }

    private AuthResponse buildAuthResponse(User user, KeycloakTokenResponse tokenResponse, boolean isNew) {
        AuthResponse.UserSummary summary = new AuthResponse.UserSummary(
                user.getId(), 
                user.getEmail(), 
                user.getUsername()
        );

        return AuthResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .user(summary)
                .isNewUser(isNew)
                .build();
    }

    /**
     * 비밀번호 재설정 인증 코드를 발송합니다.
     */
    public PasswordResetResponse sendPasswordResetCode(String email) {
        // 가입된 사용자인지 확인
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return PasswordResetResponse.failure("등록되지 않은 이메일입니다.");
        }

        // 이메일 인증 코드 발송
        var result = emailService.sendPasswordResetCode(email);
        if (result.isSuccess()) {
            return PasswordResetResponse.success(result.getMessage());
        } else {
            return PasswordResetResponse.failure(result.getMessage());
        }
    }

    /**
     * 비밀번호를 재설정합니다. (이메일 인증 코드 검증 후)
     */
    public PasswordResetResponse resetPassword(PasswordResetVerifyRequest request) {
        // 인증 코드 검증
        var verifyResult = emailService.verifyCode(request.getEmail(), request.getVerificationCode());
        if (!verifyResult.isSuccess()) {
            return PasswordResetResponse.failure(verifyResult.getMessage());
        }

        // 사용자 확인
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            return PasswordResetResponse.failure("등록되지 않은 이메일입니다.");
        }

        try {
            // Keycloak 비밀번호 재설정
            keycloakService.resetPassword(request.getEmail(), request.getNewPassword());

            // 로컬 DB 비밀번호도 업데이트
            userMapper.updatePassword(request.getEmail(), passwordEncoder.encode(request.getNewPassword()));

            // 인증 정보 삭제 (재사용 방지)
            emailVerificationMapper.deleteByEmail(request.getEmail());

            log.info("비밀번호 재설정 완료: {}", request.getEmail());
            return PasswordResetResponse.success("비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.");
        } catch (Exception e) {
            log.error("비밀번호 재설정 실패: {}", e.getMessage());
            return PasswordResetResponse.failure("비밀번호 재설정에 실패했습니다: " + e.getMessage());
        }
    }
}

