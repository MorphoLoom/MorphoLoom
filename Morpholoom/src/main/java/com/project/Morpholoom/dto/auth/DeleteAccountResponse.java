package com.project.Morpholoom.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계정 삭제 응답")
public class DeleteAccountResponse {
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 메시지", example = "계정이 성공적으로 삭제되었습니다.")
    private String message;
    
    public static DeleteAccountResponse success(String message) {
        return new DeleteAccountResponse(true, message);
    }
    
    public static DeleteAccountResponse failure(String message) {
        return new DeleteAccountResponse(false, message);
    }
}
