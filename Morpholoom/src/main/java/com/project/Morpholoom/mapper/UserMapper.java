package com.project.Morpholoom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.User;

@Mapper
public interface UserMapper {
    void insertUser(User user);

    User findByEmail(@Param("email") String email);

    User findById(@Param("id") Long id);
}

