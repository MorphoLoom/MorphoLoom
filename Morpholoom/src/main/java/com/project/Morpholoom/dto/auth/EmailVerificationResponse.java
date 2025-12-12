package com.project.Morpholoom.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 응답")
public class EmailVerificationResponse {
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 메시지", example = "인증 코드가 이메일로 발송되었습니다.")
    private String message;
    
    public static EmailVerificationResponse success(String message) {
        return new EmailVerificationResponse(true, message);
    }
    
    public static EmailVerificationResponse failure(String message) {
        return new EmailVerificationResponse(false, message);
    }
}
