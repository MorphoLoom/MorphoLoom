package com.project.Morpholoom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // REST API이므로 CSRF 비활성화
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // JWT 사용 시 STATELESS
            .authorizeHttpRequests(auth -> auth
                // Swagger UI 및 OpenAPI 문서 허용
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                // API 엔드포인트 허용
                .requestMatchers("/api/**").permitAll()
                // 정적 파일 허용
                .requestMatchers("/files/**").permitAll()
                // 나머지는 인증 필요 (필요시 수정)
                .anyRequest().authenticated()
            );

        return http.build();
    }
}

