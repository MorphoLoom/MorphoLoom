package com.project.Morpholoom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CreationLikeMapper {
    boolean exists(@Param("userId") Long userId, @Param("creationId") Long creationId);

    void insert(@Param("userId") Long userId, @Param("creationId") Long creationId);

    void delete(@Param("userId") Long userId, @Param("creationId") Long creationId);

    int countLikes(@Param("creationId") Long creationId);
}

