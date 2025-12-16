package com.project.Morpholoom.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class Creation {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String filename;
    private String thumbnail;
    private Integer likes;
    private Double rankScore;
    private LocalDateTime createdAt;
    // convenience
    private List<String> tags;
}

