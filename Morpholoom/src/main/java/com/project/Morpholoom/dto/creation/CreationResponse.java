package com.project.Morpholoom.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreationResponse {
    private String creationId;
    private String userId;
    private String imageUrl;
    private String title;
    private Integer likes;
    private Double rankScore;
}

