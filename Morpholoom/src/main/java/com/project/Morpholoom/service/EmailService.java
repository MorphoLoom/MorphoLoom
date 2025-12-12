package com.project.Morpholoom.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.project.Morpholoom.domain.EmailVerification;
import com.project.Morpholoom.dto.auth.EmailVerificationResponse;
import com.project.Morpholoom.exception.EmailVerificationException;
import com.project.Morpholoom.mapper.EmailVerificationMapper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationMapper emailVerificationMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 10;

    /**
     * 인증 코드를 생성하고 이메일로 발송합니다. (회원가입용)
     */
    public EmailVerificationResponse sendVerificationCode(String toEmail) {
        return sendVerificationCodeInternal(toEmail, "signup");
    }

    /**
     * 비밀번호 재설정을 위한 인증 코드를 발송합니다.
     */
    public EmailVerificationResponse sendPasswordResetCode(String toEmail) {
        return sendVerificationCodeInternal(toEmail, "password_reset");
    }

    /**
     * 인증 코드를 생성하고 이메일로 발송합니다.
     */
    private EmailVerificationResponse sendVerificationCodeInternal(String toEmail, String purpose) {
        try {
            // 6자리 인증 코드 생성
            String verificationCode = generateVerificationCode();
            
            // DB에 인증 정보 저장
            EmailVerification verification = new EmailVerification();
            verification.setEmail(toEmail);
            verification.setVerificationCode(verificationCode);
            verification.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
            verification.setVerified(false);
            verification.setCreatedAt(LocalDateTime.now());
            
            emailVerificationMapper.insert(verification);
            
            // 이메일 발송
            if ("password_reset".equals(purpose)) {
                sendPasswordResetEmail(toEmail, verificationCode);
            } else {
                sendEmail(toEmail, verificationCode);
            }
            
            log.info("인증 코드 발송 완료: email={}, purpose={}", toEmail, purpose);
            return EmailVerificationResponse.success("인증 코드가 이메일로 발송되었습니다. " + EXPIRATION_MINUTES + "분 내에 인증해주세요.");
            
        } catch (Exception e) {
            log.error("인증 코드 발송 실패: email={}, error={}", toEmail, e.getMessage(), e);
            return EmailVerificationResponse.failure("인증 코드 발송에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 인증 코드를 검증합니다.
     * @throws EmailVerificationException 인증 실패 시
     */
    public EmailVerificationResponse verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationMapper.findByEmailAndCode(email, code);
        
        if (verification == null) {
            throw new EmailVerificationException("INVALID_CODE", "인증 코드가 올바르지 않습니다.");
        }
        
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new EmailVerificationException("CODE_EXPIRED", "인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        
        if (verification.isVerified()) {
            throw new EmailVerificationException("ALREADY_VERIFIED", "이미 인증된 이메일입니다.");
        }
        
        // 인증 완료 처리
        emailVerificationMapper.updateVerified(email, true);
        
        log.info("이메일 인증 완료: email={}", email);
        return EmailVerificationResponse.success("이메일 인증이 완료되었습니다.");
    }

    /**
     * 이메일이 인증되었는지 확인합니다.
     */
    public boolean isEmailVerified(String email) {
        EmailVerification verification = emailVerificationMapper.findByEmail(email);
        return verification != null && verification.isVerified();
    }

    /**
     * 6자리 숫자 인증 코드를 생성합니다.
     */
    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 인증 코드 이메일을 발송합니다. (회원가입용)
     */
    private void sendEmail(String toEmail, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("[Morpholoom] 이메일 인증 코드");
        
        String htmlContent = buildEmailContent(verificationCode);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * 비밀번호 재설정 인증 코드 이메일을 발송합니다.
     */
    private void sendPasswordResetEmail(String toEmail, String verificationCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("[Morpholoom] 비밀번호 재설정 인증 코드");
        
        String htmlContent = buildPasswordResetEmailContent(verificationCode);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * 이메일 HTML 컨텐츠를 생성합니다. (회원가입용)
     */
    private String buildEmailContent(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Noto Sans KR', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; text-align: center; padding: 20px; background: white; border-radius: 10px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Morpholoom</h1>
                        <p>이메일 인증</p>
                    </div>
                    <div class="content">
                        <p>안녕하세요!</p>
                        <p>Morpholoom 회원가입을 위한 이메일 인증 코드입니다.</p>
                        <div class="code">%s</div>
                        <p>위 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>
                        <p><strong>이 코드는 %d분 후에 만료됩니다.</strong></p>
                        <p>본인이 요청하지 않은 경우, 이 이메일을 무시해주세요.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Morpholoom. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode, EXPIRATION_MINUTES);
    }

    /**
     * 비밀번호 재설정 이메일 HTML 컨텐츠를 생성합니다.
     */
    private String buildPasswordResetEmailContent(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Noto Sans KR', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #e74c3c 0%%, #c0392b 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code { font-size: 32px; font-weight: bold; color: #e74c3c; letter-spacing: 5px; text-align: center; padding: 20px; background: white; border-radius: 10px; margin: 20px 0; }
                    .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Morpholoom</h1>
                        <p>비밀번호 재설정</p>
                    </div>
                    <div class="content">
                        <p>안녕하세요!</p>
                        <p>비밀번호 재설정을 위한 인증 코드입니다.</p>
                        <div class="code">%s</div>
                        <p>위 인증 코드를 입력하여 새로운 비밀번호를 설정해주세요.</p>
                        <p><strong>이 코드는 %d분 후에 만료됩니다.</strong></p>
                        <div class="warning">
                            <strong>⚠️ 보안 안내</strong><br>
                            본인이 요청하지 않은 경우, 누군가 회원님의 계정에 접근하려는 시도일 수 있습니다.<br>
                            이 경우 이 이메일을 무시하시고, 계정 보안을 확인해주세요.
                        </div>
                    </div>
                    <div class="footer">
                        <p>© 2025 Morpholoom. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode, EXPIRATION_MINUTES);
    }
}
