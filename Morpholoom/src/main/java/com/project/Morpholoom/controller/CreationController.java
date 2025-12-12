package com.project.Morpholoom.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.project.Morpholoom.dto.common.PageResponse;
import com.project.Morpholoom.dto.creation.CreationRequest;
import com.project.Morpholoom.dto.creation.CreationResponse;
import com.project.Morpholoom.dto.creation.LikeResponse;
import com.project.Morpholoom.service.CreationService;
import com.project.Morpholoom.service.SecurityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/creations")
@RequiredArgsConstructor
@Tag(name = "Creations", description = "창작물 등록, 조회, 좋아요, 랭킹")
public class CreationController {

    private final CreationService creationService;
    private final SecurityService securityService;

    @PostMapping
    @Operation(summary = "창작물 등록", description = "이미지/비디오 정보를 포함한 창작물을 등록합니다.")
    public ResponseEntity<CreationResponse> create(
            @RequestBody CreationRequest request) {
        Long userId = securityService.getCurrentUserId();
        return ResponseEntity.ok(creationService.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "창작물 목록 조회", description = "정렬·페이지 조건으로 창작물 목록을 조회합니다.")
    public ResponseEntity<PageResponse<CreationResponse>> list(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(creationService.list(sort, page, size));
    }

    @PostMapping("/{creationId}/like")
    @Operation(summary = "좋아요 추가", description = "지정한 창작물에 좋아요를 추가합니다.")
    public ResponseEntity<LikeResponse> like(
            @PathVariable Long creationId) {
        Long userId = securityService.getCurrentUserId();
        return ResponseEntity.ok(creationService.like(userId, creationId));
    }

    @DeleteMapping("/{creationId}/like")
    @Operation(summary = "좋아요 취소", description = "지정한 창작물의 좋아요를 취소합니다.")
    public ResponseEntity<LikeResponse> unlike(
            @PathVariable Long creationId) {
        Long userId = securityService.getCurrentUserId();
        return ResponseEntity.ok(creationService.unlike(userId, creationId));
    }

    @GetMapping("/ranking")
    @Operation(summary = "랭킹 조회", description = "인기 있는 창작물 랭킹을 조회합니다.")
    public ResponseEntity<List<CreationResponse>> ranking() {
        return ResponseEntity.ok(creationService.ranking());
    }
}

