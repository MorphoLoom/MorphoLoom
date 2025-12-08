package com.project.Morpholoom.dto.creation;

import java.util.List;

import lombok.Data;

@Data
public class CreationRequest {
    private String title;
    private String imageUrl;
    private List<String> tags;
}

