package com.project.Morpholoom.controller;

import com.project.Morpholoom.dto.inference.InferenceRequest;
import com.project.Morpholoom.dto.inference.InferenceResponse;
import com.project.Morpholoom.service.DockerExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inference")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inference", description = "AI 추론 실행 API")
public class InferenceController {

    private final DockerExecutionService dockerExecutionService;

    @PostMapping("/execute")
    @Operation(summary = "AI 추론 실행", description = "Docker 컨테이너에서 Python 추론 스크립트를 실행합니다. " +
            "소스 이미지와 드라이빙 비디오를 입력으로 받아 LivePortrait 추론을 수행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추론 실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 오류 또는 컨테이너 실행 실패")
    })
    public ResponseEntity<InferenceResponse> executeInference(@RequestBody InferenceRequest request) {
        log.info("추론 실행 요청: sourcePath={}, drivingPath={}, userId={}",
                request.getSourcePath(), request.getDrivingPath(), request.getUserId());

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
            log.error("recursing_keldysh 컨테이너가 실행 중이 아닙니다.");
            return ResponseEntity.status(503).body(
                    InferenceResponse.failure(
                            "추론 서비스를 사용할 수 없습니다.",
                            "",
                            "recursing_keldysh 컨테이너가 실행 중이 아닙니다."));
        }

        try {
            InferenceResponse response = dockerExecutionService.executeInference(request);

            if (response.isSuccess()) {
                log.info("추론 실행 성공: userId={}", request.getUserId());
                return ResponseEntity.ok(response);
            } else {
                log.error("추론 실행 실패: userId={}, error={}", request.getUserId(), response.getError());
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
                            "docker ps --filter name=recursing_keldysh",
                            "컨테이너 실행 중"));
        } else {
            return ResponseEntity.status(503).body(
                    InferenceResponse.failure(
                            "추론 서비스를 사용할 수 없습니다.",
                            "docker ps --filter name=recursing_keldysh",
                            "컨테이너가 실행 중이 아닙니다."));
        }
    }
}