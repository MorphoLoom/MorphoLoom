package com.project.Morpholoom.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${storage.local.root-dir:./storage}")
    private String rootDirConfig;

    @Value("${storage.local.public-base-url:/files}")
    private String publicBaseUrl;

    private Path rootDir;

    @PostConstruct
    public void init() {
        // 상대 경로를 절대 경로로 변환
        Path configPath = Paths.get(rootDirConfig);
        if (configPath.isAbsolute()) {
            rootDir = configPath;
        } else {
            // 상대 경로인 경우 애플리케이션 실행 디렉토리 기준으로 절대 경로 생성
            rootDir = Paths.get(System.getProperty("user.dir")).resolve(configPath).normalize();
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + rootDir.toString() + "/";
        registry.addResourceHandler(publicBaseUrl + "/**")
                .addResourceLocations(location);
    }
}

