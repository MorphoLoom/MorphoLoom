package com.project.Morpholoom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.ImageAsset;

@Mapper
public interface ImageMapper {
    void insertImage(ImageAsset image);

    void deleteImage(@Param("id") Long id);

    ImageAsset findById(@Param("id") Long id);
}

