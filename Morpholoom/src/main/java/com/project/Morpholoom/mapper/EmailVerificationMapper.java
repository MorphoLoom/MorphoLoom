package com.project.Morpholoom.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.Morpholoom.domain.EmailVerification;

@Mapper
public interface EmailVerificationMapper {
    
    void insert(EmailVerification verification);
    
    EmailVerification findByEmail(@Param("email") String email);
    
    EmailVerification findByEmailAndCode(@Param("email") String email, @Param("verificationCode") String verificationCode);
    
    void updateVerified(@Param("email") String email, @Param("verified") boolean verified);
    
    void deleteByEmail(@Param("email") String email);
}
