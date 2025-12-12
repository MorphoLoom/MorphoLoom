package com.project.Morpholoom.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.project.Morpholoom.dto.auth.KeycloakTokenResponse;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(authServerUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    /**
     * Keycloak에 새 사용자를 생성합니다.
     */
    public void createUser(String email, String password, String username) {
        String adminToken = getAdminToken();

        Map<String, Object> userRepresentation = Map.of(
                "username", email,
                "email", email,
                "firstName", username,
                "enabled", true,
                "emailVerified", true,
                "requiredActions", List.of(),  // 필수 액션 없음 (비밀번호 변경 등 요구 안함)
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                ))
        );

        try {
            webClient.post()
                    .uri("/admin/realms/{realm}/users", realm)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(userRepresentation)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Keycloak 사용자 생성 성공: {}", email);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
            }
            log.error("Keycloak 사용자 생성 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Keycloak 사용자 생성 실패: " + e.getMessage());
        }
    }

    /**
     * 이메일/비밀번호로 로그인하여 토큰을 발급받습니다.
     */
    public KeycloakTokenResponse login(String email, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("username", email);
        formData.add("password", password);

        try {
            return webClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Keycloak 로그인 실패: {}", e.getResponseBodyAsString());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * Refresh Token으로 Access Token을 갱신합니다.
     */
    public KeycloakTokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            return webClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Keycloak 토큰 갱신 실패: {}", e.getResponseBodyAsString());
            throw new IllegalArgumentException("토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
        }
    }

    /**
     * 로그아웃 (토큰 무효화)
     */
    public void logout(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);

        try {
            webClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/logout", realm)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Keycloak 로그아웃 성공");
        } catch (WebClientResponseException e) {
            log.warn("Keycloak 로그아웃 실패: {}", e.getResponseBodyAsString());
        }
    }

    /**
     * Keycloak에서 사용자를 삭제합니다.
     */
    public void deleteUser(String email) {
        String adminToken = getAdminToken();
        String userId = getUserIdByEmail(email, adminToken);
        
        if (userId == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        try {
            webClient.delete()
                    .uri("/admin/realms/{realm}/users/{userId}", realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Keycloak 사용자 삭제 성공: {}", email);
        } catch (WebClientResponseException e) {
            log.error("Keycloak 사용자 삭제 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Keycloak 사용자 삭제 실패: " + e.getMessage());
        }
    }

    /**
     * Keycloak 사용자의 비밀번호를 재설정합니다.
     */
    public void resetPassword(String email, String newPassword) {
        String adminToken = getAdminToken();
        String userId = getUserIdByEmail(email, adminToken);
        
        if (userId == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        Map<String, Object> credentialRepresentation = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false
        );

        try {
            webClient.put()
                    .uri("/admin/realms/{realm}/users/{userId}/reset-password", realm, userId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(credentialRepresentation)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Keycloak 비밀번호 재설정 성공: {}", email);
        } catch (WebClientResponseException e) {
            log.error("Keycloak 비밀번호 재설정 실패: {}", e.getResponseBodyAsString());
            throw new RuntimeException("비밀번호 재설정에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이메일로 Keycloak 사용자 ID를 조회합니다.
     */
    @SuppressWarnings("unchecked")
    private String getUserIdByEmail(String email, String adminToken) {
        try {
            List<Map<String, Object>> users = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/admin/realms/{realm}/users")
                            .queryParam("email", email)
                            .queryParam("exact", true)
                            .build(realm))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (users != null && !users.isEmpty()) {
                return (String) users.get(0).get("id");
            }
            return null;
        } catch (WebClientResponseException e) {
            log.error("Keycloak 사용자 조회 실패: {}", e.getResponseBodyAsString());
            return null;
        }
    }

    /**
     * Admin API 호출을 위한 관리자 토큰을 발급받습니다.
     */
    private String getAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        log.info("Keycloak Admin 토큰 요청 - URL: {}/realms/{}/protocol/openid-connect/token, client_id: {}", 
                authServerUrl, realm, clientId);

        try {
            KeycloakTokenResponse response = webClient.post()
                    .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(KeycloakTokenResponse.class)
                    .block();

            return response != null ? response.getAccessToken() : null;
        } catch (WebClientResponseException e) {
            log.error("Keycloak Admin 토큰 발급 실패 - Status: {}, Body: {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Keycloak Admin 토큰 발급 실패: " + e.getResponseBodyAsString());
        }
    }
}
