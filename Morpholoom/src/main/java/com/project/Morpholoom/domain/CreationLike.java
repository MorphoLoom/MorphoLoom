package com.project.Morpholoom.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreationLike {
    private Long userId;
    private Long creationId;
    private LocalDateTime createdAt;
}

