package com.project.Morpholoom.dto.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadUrlResponse {
    private String uploadUrl;
    private String fileUrl;
}

