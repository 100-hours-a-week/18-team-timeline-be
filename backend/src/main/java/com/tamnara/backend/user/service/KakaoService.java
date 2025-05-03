package com.tamnara.backend.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public ResponseEntity<?> kakaoLogin(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. 토큰 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 2. 요청 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

            // 3. access_token 요청
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            // 4. access_token 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> tokenJson = objectMapper.readValue(tokenResponse.getBody(), Map.class);
            String accessToken = (String) tokenJson.get("access_token");

            // 5. 사용자 정보 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.add("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    String.class
            );

            // 6. 사용자 정보 파싱
            Map<String, Object> userInfoJson = objectMapper.readValue(userInfoResponse.getBody(), Map.class);
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoJson.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) userInfoJson.get("properties");

            String kakaoId = String.valueOf(userInfoJson.get("id"));
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) properties.get("nickname");

            return ResponseEntity.ok().body(
                    "✅ 카카오 로그인 성공\n\n닉네임: " + nickname + "\n이메일: " + email + "\n카카오 ID: " + kakaoId
            );

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ JSON 파싱 오류: " + e.getMessage());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("❌ 카카오 서버 요청 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ 예상치 못한 오류 발생: " + e.getMessage());
        }
    }
}
