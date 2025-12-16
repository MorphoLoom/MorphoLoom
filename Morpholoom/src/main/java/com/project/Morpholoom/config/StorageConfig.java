package com.project.Morpholoom.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        // /files/** -> ./storage/ 매핑
        String location = "file:" + rootDir.toString() + "/";
        registry.addResourceHandler(publicBaseUrl + "/**")
                .addResourceLocations(location);
        
        // /data/images/** -> /app/src/src/data/images/ 매핑 (썸네일 이미지용)
        // 절대 경로 사용으로 변경
        String dataImagesLocation = "file:/app/src/src/data/images/";
        log.info("Resource handler /data/images/** -> {}", dataImagesLocation);
        registry.addResourceHandler("/data/images/**")
                .addResourceLocations(dataImagesLocation);
        
        // /data/videos/** -> /app/src/src/data/videos/ 매핑 (동영상 파일용)
        String dataVideosLocation = "file:/app/src/src/data/videos/";
        log.info("Resource handler /data/videos/** -> {}", dataVideosLocation);
        registry.addResourceHandler("/data/videos/**")
                .addResourceLocations(dataVideosLocation);
        
        // 기본 static 리소스 (classpath:/static/) 명시적 설정 - 가장 마지막에 등록
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}

