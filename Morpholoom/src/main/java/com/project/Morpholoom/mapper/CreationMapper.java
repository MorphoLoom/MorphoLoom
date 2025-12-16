package com.project.Morpholoom.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.Creation;
import com.project.Morpholoom.dto.creation.CreationDetailResponse;

@Mapper
public interface CreationMapper {
    void insertCreation(Creation creation);

    CreationDetailResponse findDetailById(@Param("id") Long id);

    List<Creation> listCreations(@Param("sort") String sort,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    List<Creation> listByUserId(@Param("userId") Long userId,
                                @Param("sort") String sort,
                                @Param("limit") int limit,
                                @Param("offset") int offset);

    List<Creation> listLikedByUserId(@Param("userId") Long userId,
                                     @Param("sort") String sort,
                                     @Param("limit") int limit,
                                     @Param("offset") int offset);

    List<Creation> ranking(@Param("limit") int limit);

    Creation findById(@Param("id") Long id);

    void updateLikes(@Param("id") Long id, @Param("likes") int likes);

    void deleteById(@Param("id") Long id);
}

