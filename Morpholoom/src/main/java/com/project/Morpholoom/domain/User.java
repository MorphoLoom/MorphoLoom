package com.project.Morpholoom.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String email;
    private String passwordHash;
    private String username;
    private String socialProvider;
    private LocalDateTime createdAt;
}

