package com.project.Morpholoom.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "이메일 인증 코드 발송 요청")
public class SendVerificationRequest {
    
    @Schema(description = "인증 코드를 받을 이메일 주소", example = "user@example.com")
    private String email;
}
