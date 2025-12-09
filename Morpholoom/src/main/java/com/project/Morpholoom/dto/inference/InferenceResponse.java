package com.project.Morpholoom.dto.inference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추론 응답 DTO")
public class InferenceResponse {

    @Schema(description = "추론 실행 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "추론이 성공적으로 실행되었습니다.")
    private String message;

    @Schema(description = "실행된 명령어", example = "python inference.py -s /app/storage/user/1/images/test.png -d /app/storage/user/1/videos/test.mp4")
    private String executedCommand;

    @Schema(description = "명령어 실행 결과 출력")
    private String output;

    @Schema(description = "오류 메시지 (실패 시)")
    private String error;

    // 성공 응답 생성 메소드
    public static InferenceResponse success(String message, String command, String output) {
        InferenceResponse response = new InferenceResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setExecutedCommand(command);
        response.setOutput(output);
        return response;
    }

    // 실패 응답 생성 메소드
    public static InferenceResponse failure(String message, String command, String error) {
        InferenceResponse response = new InferenceResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setExecutedCommand(command);
        response.setError(error);
        return response;
    }
}