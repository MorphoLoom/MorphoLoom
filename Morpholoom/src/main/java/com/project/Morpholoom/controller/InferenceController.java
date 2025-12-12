package com.project.Morpholoom.controller;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.Morpholoom.dto.inference.InferenceRequest;
import com.project.Morpholoom.dto.inference.InferenceResponse;
import com.project.Morpholoom.service.DockerExecutionService;
import com.project.Morpholoom.service.SecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/inference")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inference", description = "AI 추론 실행 API")
public class InferenceController {

    private final DockerExecutionService dockerExecutionService;
    private final SecurityService securityService;

    @PostMapping("/execute")
    @Operation(summary = "AI 추론 실행", description = "Docker 컨테이너에서 Python 추론 스크립트를 실행합니다. " +
            "소스 이미지와 드라이빙 비디오를 입력으로 받아 LivePortrait 추론을 수행하고 결과 동영상을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추론 실행 성공 - 동영상 파일 반환"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 컨테이너 실행 실패")
    })
    public ResponseEntity<?> executeInference(@RequestBody InferenceRequest request) {
        Long userId = securityService.getCurrentUserId();
        log.info("추론 실행 요청: sourcePath={}, drivingPath={}, userId={}",
                request.getSourcePath(), request.getDrivingPath(), userId);

        // 입력 유효성 검사
        if (request.getSourcePath() == null || request.getSourcePath().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    InferenceResponse.failure("소스 이미지 경로가 필요합니다.", "", "소스 이미지 경로가 비어있습니다."));
        }

        if (request.getDrivingPath() == null || request.getDrivingPath().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    InferenceResponse.failure("드라이빙 비디오 경로가 필요합니다.", "", "드라이빙 비디오 경로가 비어있습니다."));
        }

        // 컨테이너 상태 확인
        if (!dockerExecutionService.isContainerRunning()) {
            log.error("liveportrait-app 컨테이너가 실행 중이 아닙니다.");
            return ResponseEntity.status(503).body(
                    InferenceResponse.failure(
                            "추론 서비스를 사용할 수 없습니다.",
                            "",
                            "liveportrait-app 컨테이너가 실행 중이 아닙니다."));
        }

        try {
            InferenceResponse response = dockerExecutionService.executeInference(request, userId);

            if (response.isSuccess()) {
                log.info("추론 실행 성공: userId={}, resultPath={}", userId, response.getResultVideoPath());
                
                // 결과 동영상 파일 반환
                Path videoPath = Paths.get(response.getResultVideoPath());
                Resource videoResource = new UrlResource(videoPath.toUri());
                
                if (!videoResource.exists() || !videoResource.isReadable()) {
                    log.error("결과 동영상 파일을 찾을 수 없습니다: {}", response.getResultVideoPath());
                    return ResponseEntity.status(500).body(
                            InferenceResponse.failure(
                                    "결과 동영상 파일을 찾을 수 없습니다.",
                                    response.getExecutedCommand(),
                                    "파일 경로: " + response.getResultVideoPath()));
                }
                
                String filename = videoPath.getFileName().toString();
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(videoResource);
            } else {
                log.error("추론 실행 실패: userId={}, error={}", userId, response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("추론 실행 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    InferenceResponse.failure(
                            "추론 실행 중 서버 오류가 발생했습니다.",
                            "",
                            e.getMessage()));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "추론 서비스 상태 확인", description = "Docker 컨테이너 상태를 확인하여 추론 서비스 사용 가능 여부를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 확인 성공"),
            @ApiResponse(responseCode = "503", description = "서비스 사용 불가")
    })
    public ResponseEntity<InferenceResponse> checkStatus() {
        boolean isRunning = dockerExecutionService.isContainerRunning();

        if (isRunning) {
            return ResponseEntity.ok(
                    InferenceResponse.success(
                            "추론 서비스가 정상적으로 실행 중입니다.",
                            "docker ps --filter name=liveportrait-app",
                            ""));
        } else {
            return ResponseEntity.status(503).body(
                    InferenceResponse.failure(
                            "추론 서비스를 사용할 수 없습니다.",
                            "docker ps --filter name=liveportrait-app",
                            "컨테이너가 실행 중이 아닙니다."));
        }
    }
}