package com.project.Morpholoom.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "이메일 인증 코드 확인 요청")
public class EmailVerificationRequest {
    
    @Schema(description = "인증할 이메일 주소", example = "user@example.com")
    private String email;
    
    @Schema(description = "인증 코드", example = "123456")
    private String verificationCode;
}
