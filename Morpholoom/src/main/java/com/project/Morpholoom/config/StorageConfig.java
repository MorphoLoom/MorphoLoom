package com.project.Morpholoom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StorageConfig implements WebMvcConfigurer {

    @Value("${storage.local.root-dir:./storage}")
    private String rootDir;

    @Value("${storage.local.public-base-url:/files}")
    private String publicBaseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + rootDir + "/";
        registry.addResourceHandler(publicBaseUrl + "/**")
                .addResourceLocations(location);
    }
}

