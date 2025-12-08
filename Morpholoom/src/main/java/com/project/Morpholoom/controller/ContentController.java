package com.project.Morpholoom.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.Morpholoom.dto.content.ImageResponse;
import com.project.Morpholoom.dto.content.ImageSaveRequest;
import com.project.Morpholoom.dto.content.UploadUrlRequest;
import com.project.Morpholoom.dto.content.UploadUrlResponse;
import com.project.Morpholoom.dto.content.VideoResponse;
import com.project.Morpholoom.dto.content.VideoSaveRequest;
import com.project.Morpholoom.service.ContentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "이미지/비디오 업로드 URL 발급 및 메타데이터 저장")
public class ContentController {

    private final ContentService contentService;

    @PostMapping("/images/upload-url")
    @Operation(summary = "이미지 업로드 URL 발급", description = "이미지 업로드를 위한 사전 서명 URL을 발급합니다.")
    public ResponseEntity<UploadUrlResponse> issueImageUploadUrl(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody UploadUrlRequest request) {
        return ResponseEntity.ok(contentService.issueImageUploadUrl(userId, request));
    }

    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "멀티파트 파일을 서버 파일시스템에 저장하고 메타데이터를 생성합니다.")
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(contentService.uploadImage(userId, file));
    }

    @PostMapping("/images")
    @Operation(summary = "이미지 메타 저장", description = "이미지 파일 업로드 후 메타데이터를 저장합니다.")
    public ResponseEntity<ImageResponse> saveImageMeta(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody ImageSaveRequest request) {
        return ResponseEntity.ok(contentService.saveImageMeta(userId, request));
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "이미지 삭제", description = "저장된 이미지 메타데이터를 삭제합니다.")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        contentService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/videos/upload-url")
    @Operation(summary = "비디오 업로드 URL 발급", description = "비디오 업로드를 위한 사전 서명 URL을 발급합니다.")
    public ResponseEntity<UploadUrlResponse> issueVideoUploadUrl(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody UploadUrlRequest request) {
        return ResponseEntity.ok(contentService.issueVideoUploadUrl(userId, request));
    }

    @PostMapping(value = "/videos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "비디오 업로드", description = "멀티파트 파일을 서버 파일시스템에 저장하고 메타데이터를 생성합니다.")
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(contentService.uploadVideo(userId, file));
    }

    @PostMapping("/videos")
    @Operation(summary = "비디오 메타 저장", description = "비디오 파일 업로드 후 메타데이터를 저장합니다.")
    public ResponseEntity<VideoResponse> saveVideoMeta(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody VideoSaveRequest request) {
        return ResponseEntity.ok(contentService.saveVideoMeta(userId, request));
    }

    @DeleteMapping("/videos/{videoId}")
    @Operation(summary = "비디오 삭제", description = "저장된 비디오 메타데이터를 삭제합니다.")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId) {
        contentService.deleteVideo(videoId);
        return ResponseEntity.noContent().build();
    }
}

