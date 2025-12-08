package com.project.Morpholoom.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ImageAsset {
    private Long id;
    private Long userId;
    private String fileUrl;
    private LocalDateTime createdAt;
}

