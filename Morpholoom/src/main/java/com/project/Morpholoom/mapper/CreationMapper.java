package com.project.Morpholoom.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.Creation;

@Mapper
public interface CreationMapper {
    void insertCreation(Creation creation);

    List<Creation> listCreations(@Param("sort") String sort,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    List<Creation> ranking(@Param("limit") int limit);

    Creation findById(@Param("id") Long id);

    void updateLikes(@Param("id") Long id, @Param("likes") int likes);
}

