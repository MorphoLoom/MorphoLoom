package com.project.Morpholoom.dto.inference;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추론 요청 DTO")
public class InferenceRequest {

    @Schema(description = "소스 이미지 파일 경로", example = "/app/storage/user/1/images/test.png")
    private String sourcePath;

    @Schema(description = "드라이빙 비디오 파일 경로", example = "/app/storage/user/1/videos/test.mp4")
    private String drivingPath;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
}