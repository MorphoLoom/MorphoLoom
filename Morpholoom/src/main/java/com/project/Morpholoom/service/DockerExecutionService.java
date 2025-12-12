package com.project.Morpholoom.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.project.Morpholoom.dto.inference.InferenceRequest;
import com.project.Morpholoom.dto.inference.InferenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class DockerExecutionService {

    private static final String CONTAINER_NAME = "liveportrait-app";
    private static final int TIMEOUT_MINUTES = 10;

    private final DockerClient dockerClient;

    /**
     * Docker 컨테이너에서 Python 추론 명령을 실행합니다.
     *
     * @param request 추론 요청 정보
     * @param userId 사용자 ID
     * @return 추론 실행 결과
     */
    public InferenceResponse executeInference(InferenceRequest request, Long userId) {
        String sourceFileName = extractFileNameWithoutExtension(request.getSourcePath());
        String drivingFileName = extractFileNameWithoutExtension(request.getDrivingPath());
        
        String sourcePath = String.format("/app/assets/examples/source/%d/%s", userId, extractFileName(request.getSourcePath()));
        String drivingPath = String.format("/app/assets/examples/driving/%d/%s", userId, extractFileName(request.getDrivingPath()));

        // 결과 동영상 파일 경로 생성: {sourceName}--{drivingName}.mp4
        // String resultVideoPath = String.format("/app/src/storage/user/results/%d/%s--%s.mp4", 
        //         userId, sourceFileName, drivingFileName);
        String resultVideoPath = String.format("/app/src/storage/user/results/%s--%s.mp4", 
                 sourceFileName, drivingFileName);
        String[] command = {
                "python", "/app/inference.py",
                "-s", sourcePath,
                "-d", drivingPath
        };

        String commandStr = String.join(" ", command);
        log.info("Docker exec 명령 실행: container={}, command={}", CONTAINER_NAME, commandStr);

        try {
            // 컨테이너 ID 조회
            String containerId = getContainerId();
            if (containerId == null) {
                return InferenceResponse.failure(
                        "컨테이너를 찾을 수 없습니다.",
                        commandStr,
                        "컨테이너 '" + CONTAINER_NAME + "'가 존재하지 않습니다.");
            }

            // exec 명령 생성
            ExecCreateCmdResponse execCreateResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(command)
                    .exec();

            // 출력 스트림 준비
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            // exec 명령 실행
            ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);
            dockerClient.execStartCmd(execCreateResponse.getId())
                    .exec(callback)
                    .awaitCompletion(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            String stdoutStr = stdout.toString();
            String stderrStr = stderr.toString();

            // 실행 결과 확인
            Long exitCode = dockerClient.inspectExecCmd(execCreateResponse.getId()).exec().getExitCodeLong();

            if (exitCode != null && exitCode == 0) {
                log.info("Docker exec 명령 실행 성공: {}, 결과 파일: {}", commandStr, resultVideoPath);
                return InferenceResponse.success(
                        "추론이 성공적으로 실행되었습니다.",
                        commandStr,
                        resultVideoPath);
            } else {
                log.error("Docker exec 명령 실행 실패 (exitCode: {}): {}", exitCode, commandStr);
                return InferenceResponse.failure(
                        "추론 실행 중 오류가 발생했습니다.",
                        commandStr,
                        stderrStr.isEmpty() ? stdoutStr : stderrStr);
            }

        } catch (NotFoundException e) {
            log.error("컨테이너를 찾을 수 없습니다: {}", e.getMessage(), e);
            return InferenceResponse.failure(
                    "컨테이너를 찾을 수 없습니다.",
                    commandStr,
                    e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Docker exec 명령 실행이 중단되었습니다: {}", e.getMessage(), e);
            return InferenceResponse.failure(
                    "명령 실행이 중단되었습니다.",
                    commandStr,
                    e.getMessage());
        } catch (Exception e) {
            log.error("Docker exec 명령 실행 중 오류 발생: {}", e.getMessage(), e);
            return InferenceResponse.failure(
                    "명령 실행 중 오류가 발생했습니다.",
                    commandStr,
                    e.getMessage());
        }
    }

    /**
     * 파일 경로에서 파일명만 추출합니다.
     */
    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    /**
     * 파일 경로에서 확장자를 제외한 파일명만 추출합니다.
     */
    private String extractFileNameWithoutExtension(String path) {
        String fileName = extractFileName(path);
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    /**
     * 컨테이너 이름으로 컨테이너 ID를 조회합니다.
     */
    private String getContainerId() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withNameFilter(List.of(CONTAINER_NAME))
                .withShowAll(true)
                .exec();

        if (containers.isEmpty()) {
            return null;
        }
        return containers.get(0).getId();
    }

    /**
     * 지정된 컨테이너가 실행 중인지 확인합니다.
     *
     * @return 컨테이너 실행 상태
     */
    public boolean isContainerRunning() {
        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withNameFilter(List.of(CONTAINER_NAME))
                    .withShowAll(false) // 실행 중인 컨테이너만
                    .exec();

            boolean running = !containers.isEmpty();
            log.debug("컨테이너 {} 상태: {}", CONTAINER_NAME, running ? "실행 중" : "중지됨");
            return running;
        } catch (Exception e) {
            log.error("컨테이너 상태 확인 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
}