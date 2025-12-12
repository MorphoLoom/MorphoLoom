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

    @Schema(description = "소스 파일 ", example = "test.png")
    private String sourcePath;

    @Schema(description = "타겟 파일 ", example = "test.mp4")
    private String drivingPath;
}