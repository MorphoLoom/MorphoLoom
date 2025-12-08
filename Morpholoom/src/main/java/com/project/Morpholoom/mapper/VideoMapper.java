package com.project.Morpholoom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.VideoAsset;

@Mapper
public interface VideoMapper {
    void insertVideo(VideoAsset video);

    void deleteVideo(@Param("id") Long id);

    VideoAsset findById(@Param("id") Long id);
}

