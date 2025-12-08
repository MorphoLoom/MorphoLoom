package com.project.Morpholoom.dto.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private int page;
    private int size;
    private List<T> items;
}

