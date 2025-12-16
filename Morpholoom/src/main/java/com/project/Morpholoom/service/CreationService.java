package com.project.Morpholoom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.Morpholoom.domain.Creation;
import com.project.Morpholoom.dto.common.PageResponse;
import com.project.Morpholoom.dto.creation.CreationDetailResponse;
import com.project.Morpholoom.dto.creation.CreationRequest;
import com.project.Morpholoom.dto.creation.CreationResponse;
import com.project.Morpholoom.dto.creation.LikeResponse;
import com.project.Morpholoom.mapper.CreationLikeMapper;
import com.project.Morpholoom.mapper.CreationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreationService {

    private final CreationMapper creationMapper;
    private final CreationLikeMapper likeMapper;

    @Value("${app.host-ip:localhost}")
    private String hostIp;

    @Value("${app.host-port:8888}")
    private int serverPort;

    public CreationResponse create(Long userId, CreationRequest request) {
        // filename에서 확장자 제거하여 thumbnail 파일명 생성
        String filenameWithoutExt = request.getFilename();
        if (filenameWithoutExt != null && filenameWithoutExt.contains(".")) {
            filenameWithoutExt = filenameWithoutExt.substring(0, filenameWithoutExt.lastIndexOf('.'));
        }
        String thumbnail = filenameWithoutExt + "_thumbnail.jpg";

        Creation creation = new Creation();
        creation.setUserId(userId);
        creation.setTitle(request.getTitle());
        creation.setDescription(request.getDescription());
        creation.setFilename(request.getFilename());
        creation.setThumbnail(thumbnail);
        creation.setLikes(0);
        creation.setRankScore(0.0);
        creation.setCreatedAt(LocalDateTime.now());
        creationMapper.insertCreation(creation);

        return CreationResponse.builder()
                .id(String.valueOf(creation.getId()))
                .userId(String.valueOf(userId))
                .title(creation.getTitle())
                .description(creation.getDescription())
                .filename(creation.getFilename())
                .thumbnail(creation.getThumbnail())
                .likes(creation.getLikes())
                .rankScore(creation.getRankScore())
                .createdAt(creation.getCreatedAt())
                .build();
    }

    public PageResponse<CreationResponse> list(String sort, int page, int size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Creation> rows = creationMapper.listCreations(sort, size, offset);
        List<CreationResponse> items = rows.stream()
                .map(this::toResponseWithThumbnailUrl)
                .toList();
        return new PageResponse<>(page, size, items);
    }

    public PageResponse<CreationResponse> listByUserId(Long userId, String sort, int page, int size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Creation> rows = creationMapper.listByUserId(userId, sort, size, offset);
        List<CreationResponse> items = rows.stream()
                .map(this::toResponseWithThumbnailUrl)
                .toList();
        return new PageResponse<>(page, size, items);
    }

    public PageResponse<CreationResponse> listLikedByUserId(Long userId, String sort, int page, int size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Creation> rows = creationMapper.listLikedByUserId(userId, sort, size, offset);
        List<CreationResponse> items = rows.stream()
                .map(this::toResponseWithThumbnailUrl)
                .toList();
        return new PageResponse<>(page, size, items);
    }

    public void delete(Long userId, Long creationId) {
        Creation creation = creationMapper.findById(creationId);
        if (creation == null) {
            throw new IllegalArgumentException("창작물을 찾을 수 없습니다.");
        }
        if (!creation.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 창작물만 삭제할 수 있습니다.");
        }
        likeMapper.deleteByCreationId(creationId);
        creationMapper.deleteById(creationId);
    }

    public List<CreationResponse> ranking() {
        return creationMapper.ranking(100).stream()
                .map(this::toResponseWithThumbnailUrl)
                .toList();
    }

    public CreationDetailResponse getDetail(Long creationId, Long userId) {
        CreationDetailResponse detail = creationMapper.findDetailById(creationId);
        if (detail == null) {
            throw new IllegalArgumentException("창작물을 찾을 수 없습니다.");
        }
        // 동영상 URL 생성
        if (detail.getFilename() != null) {
            String videoUrl = String.format("http://%s:%d/data/videos/%s", hostIp, serverPort, detail.getFilename());
            detail.setVideoUrl(videoUrl);
        }
        // 좋아요 여부 확인
        boolean liked = likeMapper.exists(userId, creationId);
        detail.setLiked(liked);
        return detail;
    }

    private CreationResponse toResponse(Creation c) {
        return CreationResponse.builder()
                .id(String.valueOf(c.getId()))
                .userId(String.valueOf(c.getUserId()))
                .title(c.getTitle())
                .description(c.getDescription())
                .filename(c.getFilename())
                .thumbnail(c.getThumbnail())
                .likes(c.getLikes())
                .rankScore(c.getRankScore())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private CreationResponse toResponseWithThumbnailUrl(Creation c) {
        String thumbnailUrl = String.format("http://%s:%d/data/images/%s", hostIp, serverPort, c.getThumbnail());
        return CreationResponse.builder()
                .id(String.valueOf(c.getId()))
                .userId(String.valueOf(c.getUserId()))
                .title(c.getTitle())
                .description(c.getDescription())
                .filename(c.getFilename())
                .thumbnail(thumbnailUrl)
                .likes(c.getLikes())
                .rankScore(c.getRankScore())
                .createdAt(c.getCreatedAt())
                .build();
    }

    public LikeResponse like(Long userId, Long creationId) {
        if (!likeMapper.exists(userId, creationId)) {
            likeMapper.insert(userId, creationId);
        }
        int count = likeMapper.countLikes(creationId);
        creationMapper.updateLikes(creationId, count);
        return new LikeResponse(true, count);
    }

    public LikeResponse unlike(Long userId, Long creationId) {
        if (likeMapper.exists(userId, creationId)) {
            likeMapper.delete(userId, creationId);
        }
        int count = likeMapper.countLikes(creationId);
        creationMapper.updateLikes(creationId, count);
        return new LikeResponse(false, count);
    }
}

