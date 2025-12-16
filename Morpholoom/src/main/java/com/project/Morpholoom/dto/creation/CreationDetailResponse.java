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
public class CreationDetailResponse {
    private Long creationId;
    private String title;
    private Integer likes;
    private LocalDateTime createdAt;
    private String description;
    private String username;
    private String filename;
    private String videoUrl;
    private Boolean liked;
}
