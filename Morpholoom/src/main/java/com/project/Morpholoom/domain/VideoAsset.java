package com.project.Morpholoom.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VideoAsset {
    private Long id;
    private Long userId;
    private String fileUrl;
    private LocalDateTime createdAt;
}

