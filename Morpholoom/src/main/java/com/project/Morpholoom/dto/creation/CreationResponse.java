package com.project.Morpholoom.dto.creation;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreationResponse {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String filename;
    private Integer likes;
    private Double rankScore;
    private LocalDateTime createdAt;
}

