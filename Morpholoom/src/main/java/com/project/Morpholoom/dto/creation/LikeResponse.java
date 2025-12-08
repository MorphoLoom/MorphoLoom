package com.project.Morpholoom.dto.creation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeResponse {
    private boolean liked;
    private int likes;
}

