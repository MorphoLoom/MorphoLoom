package com.project.Morpholoom.dto.content;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private String videoId;
    private String fileUrl;
    private LocalDateTime createdAt;
}

