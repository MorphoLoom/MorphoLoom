package com.project.Morpholoom.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.Morpholoom.domain.ImageAsset;
import com.project.Morpholoom.domain.VideoAsset;
import com.project.Morpholoom.dto.content.ImageResponse;
import com.project.Morpholoom.dto.content.ImageSaveRequest;
import com.project.Morpholoom.dto.content.VideoResponse;
import com.project.Morpholoom.dto.content.VideoSaveRequest;
import com.project.Morpholoom.mapper.ImageMapper;
import com.project.Morpholoom.mapper.VideoMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ImageMapper imageMapper;
    private final VideoMapper videoMapper;

    @Value("${storage.local.root-dir:./storage}")
    private String rootDirConfig;
    
    private Path rootDir;

    @Value("${storage.local.public-base-url:/files}")
    private String publicBaseUrl;

    @PostConstruct
    public void init() {
        // 상대 경로를 절대 경로로 변환
        Path configPath = Paths.get(rootDirConfig);
        if (configPath.isAbsolute()) {
            rootDir = configPath;
        } else {
            // 상대 경로인 경우 애플리케이션 실행 디렉토리 기준으로 절대 경로 생성
            rootDir = Paths.get(System.getProperty("user.dir")).resolve(configPath).normalize();
        }
    }

    public ImageResponse uploadImage(Long userId, MultipartFile file) throws IOException {
        StoredFile stored = storeFile(userId, "images", file);
        return saveImageMeta(userId, stored.fileUrl());
    }

    public ImageResponse saveImageMeta(Long userId, ImageSaveRequest request) {
        return saveImageMeta(userId, request.getFileUrl());
    }

    private ImageResponse saveImageMeta(Long userId, String fileUrl) {
        ImageAsset image = new ImageAsset();
        image.setUserId(userId);
        image.setFileUrl(fileUrl);
        image.setCreatedAt(LocalDateTime.now());
        imageMapper.insertImage(image);
        return new ImageResponse(String.valueOf(image.getId()), image.getFileUrl(), image.getCreatedAt());
    }

    public VideoResponse uploadVideo(Long userId, MultipartFile file) throws IOException {
        StoredFile stored = storeFile(userId, "videos", file);
        return saveVideoMeta(userId, stored.fileUrl());
    }

    public VideoResponse saveVideoMeta(Long userId, VideoSaveRequest request) {
        return saveVideoMeta(userId, request.getFileUrl());
    }

    private VideoResponse saveVideoMeta(Long userId, String fileUrl) {
        VideoAsset video = new VideoAsset();
        video.setUserId(userId);
        video.setFileUrl(fileUrl);
        video.setCreatedAt(LocalDateTime.now());
        videoMapper.insertVideo(video);
        return new VideoResponse(String.valueOf(video.getId()), video.getFileUrl(), video.getCreatedAt());
    }


    private StoredFile storeFile(Long userId, String category, MultipartFile file) throws IOException {
        String originalName = sanitize(file.getOriginalFilename());
        String filename = (StringUtils.hasText(originalName) ? originalName : category);
        Path targetDir = rootDir.resolve("user").resolve(category).resolve(String.valueOf(userId));
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve(filename);
        file.transferTo(targetFile.toFile());
        String fileUrl = publicBaseUrl + "/" + "user/" + category + "/" + userId + "/" + filename;
        return new StoredFile(targetFile, fileUrl);
    }

    private String sanitize(String name) {
        return StringUtils.hasText(name) ? StringUtils.getFilename(name) : "file";
    }

    private record StoredFile(Path path, String fileUrl) {
    }
}

