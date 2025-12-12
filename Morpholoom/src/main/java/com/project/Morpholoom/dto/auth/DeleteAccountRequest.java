package com.project.Morpholoom.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "계정 삭제 요청")
public class DeleteAccountRequest {
    
    @Schema(description = "삭제할 계정의 이메일", example = "user@example.com")
    private String email;
    
    @Schema(description = "비밀번호 확인", example = "password123")
    private String password;
}
