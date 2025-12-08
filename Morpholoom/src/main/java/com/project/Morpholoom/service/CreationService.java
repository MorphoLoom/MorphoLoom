package com.project.Morpholoom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.Morpholoom.domain.Creation;
import com.project.Morpholoom.dto.common.PageResponse;
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

    public CreationResponse create(Long userId, CreationRequest request) {
        Creation creation = new Creation();
        creation.setUserId(userId);
        creation.setTitle(request.getTitle());
        creation.setImageUrl(request.getImageUrl());
        creation.setLikes(0);
        creation.setRankScore(0.0);
        creation.setCreatedAt(LocalDateTime.now());
        creationMapper.insertCreation(creation);

        return new CreationResponse(String.valueOf(creation.getId()), String.valueOf(userId),
                creation.getImageUrl(), creation.getTitle(), creation.getLikes(), creation.getRankScore());
    }

    public PageResponse<CreationResponse> list(String sort, int page, int size) {
        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        List<Creation> rows = creationMapper.listCreations(sort, size, offset);
        List<CreationResponse> items = rows.stream()
                .map(c -> new CreationResponse(String.valueOf(c.getId()), String.valueOf(c.getUserId()),
                        c.getImageUrl(), c.getTitle(), c.getLikes(), c.getRankScore()))
                .toList();
        return new PageResponse<>(page, size, items);
    }

    public List<CreationResponse> ranking() {
        return creationMapper.ranking(100).stream()
                .map(c -> new CreationResponse(String.valueOf(c.getId()), String.valueOf(c.getUserId()),
                        c.getImageUrl(), c.getTitle(), c.getLikes(), c.getRankScore()))
                .toList();
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

