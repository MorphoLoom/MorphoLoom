package com.project.Morpholoom.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.Morpholoom.dto.content.ImageResponse;
import com.project.Morpholoom.dto.content.VideoResponse;
import com.project.Morpholoom.service.ContentService;
import com.project.Morpholoom.service.SecurityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "이미지/비디오 업로드 URL 발급 및 메타데이터 저장")
public class ContentController {

    private final ContentService contentService;
    private final SecurityService securityService;

    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "멀티파트 파일을 서버 파일시스템에 저장하고 메타데이터를 생성합니다.")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestPart("file") MultipartFile file) throws IOException {
        Long userId = securityService.getCurrentUserId();
        return ResponseEntity.ok(contentService.uploadImage(userId, file));
    }

    @PostMapping(value = "/videos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "비디오 업로드", description = "멀티파트 파일을 서버 파일시스템에 저장하고 메타데이터를 생성합니다.")
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestPart("file") MultipartFile file) throws IOException {
        Long userId = securityService.getCurrentUserId();
        return ResponseEntity.ok(contentService.uploadVideo(userId, file));
    }
}

