package com.project.Morpholoom.service;

import com.project.Morpholoom.dto.inference.InferenceRequest;
import com.project.Morpholoom.dto.inference.InferenceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DockerExecutionService {

    private static final String CONTAINER_NAME = "recursing_keldysh";
    private static final int TIMEOUT_MINUTES = 10;

    /**
     * Docker 컨테이너에서 Python 추론 명령을 실행합니다.
     *
     * @param request 추론 요청 정보
     * @return 추론 실행 결과
     */
    public InferenceResponse executeInference(InferenceRequest request) {
        String command = buildDockerCommand(request);
        log.info("Docker 명령 실행: {}", command);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("Docker 실행 출력: {}", line);
                }
            }

            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                log.error("Docker 명령 실행 타임아웃: {}", command);
                return InferenceResponse.failure(
                        "명령 실행이 타임아웃되었습니다.",
                        command,
                        "타임아웃 발생 (" + TIMEOUT_MINUTES + "분)");
            }

            int exitCode = process.exitValue();
            String outputStr = output.toString();

            if (exitCode == 0) {
                log.info("Docker 명령 실행 성공: {}", command);
                return InferenceResponse.success(
                        "추론이 성공적으로 실행되었습니다.",
                        command,
                        outputStr);
            } else {
                log.error("Docker 명령 실행 실패 (exitCode: {}): {}", exitCode, command);
                return InferenceResponse.failure(
                        "추론 실행 중 오류가 발생했습니다.",
                        command,
                        outputStr);
            }

        } catch (IOException e) {
            log.error("Docker 명령 실행 중 IO 오류 발생: {}", e.getMessage(), e);
            return InferenceResponse.failure(
                    "명령 실행 중 IO 오류가 발생했습니다.",
                    command,
                    e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Docker 명령 실행이 중단되었습니다: {}", e.getMessage(), e);
            return InferenceResponse.failure(
                    "명령 실행이 중단되었습니다.",
                    command,
                    e.getMessage());
        }
    }

    /**
     * Docker exec 명령을 생성합니다.
     *
     * @param request 추론 요청 정보
     * @return Docker exec 명령 문자열
     */
    private String buildDockerCommand(InferenceRequest request) {
        return String.format(
                "docker exec %s python inference.py -s %s -d %s",
                CONTAINER_NAME,
                request.getSourcePath(),
                request.getDrivingPath());
    }

    /**
     * 지정된 컨테이너가 실행 중인지 확인합니다.
     *
     * @return 컨테이너 실행 상태
     */
    public boolean isContainerRunning() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "ps", "--filter", "name=" + CONTAINER_NAME, "--format", "{{.Status}}");

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String status = reader.readLine();
                boolean running = status != null && status.startsWith("Up");
                log.debug("컨테이너 {} 상태: {}", CONTAINER_NAME, running ? "실행 중" : "중지됨");
                return running;
            }
        } catch (IOException e) {
            log.error("컨테이너 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}