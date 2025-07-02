package com.tamnara.backend.auth.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.auth.constant.KakaoOAuthConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.EXTERNAL_API_TIMEOUT;
import static com.tamnara.backend.auth.constant.AuthResponseMessage.PARSING_ACCESS_TOKEN_FAILS;
import static com.tamnara.backend.auth.constant.AuthResponseMessage.PARSING_USER_INFO_FAILS;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiClientImpl implements KakaoApiClient {

    @Value("${kakao.client-id}") private String clientId;
    @Value("${kakao.redirect-uri}") private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpEntity<?> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KakaoOAuthConstant.KAKAO_OAUTH_ACCESS_TOKEN,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            return (String) body.get("access_token");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[ERROR] 카카오 응답 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("카카오 요청 실패", e);
        } catch (ResourceAccessException e) {
            log.error("[ERROR] 카카오 연결 오류: {}", e.getMessage());
            throw new RuntimeException(EXTERNAL_API_TIMEOUT, e);
        } catch (JsonProcessingException e) {
            log.error("[ERROR] 카카오 토큰 파싱 실패", e);
            throw new RuntimeException(PARSING_ACCESS_TOKEN_FAILS, e);
        } catch (Exception e) {
            log.error("[ERROR] 알 수 없는 예외 발생", e);
            throw new RuntimeException("알 수 없는 오류", e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                KakaoOAuthConstant.KAKAO_OAUTH_USER_INFO,
                HttpMethod.GET,
                request,
                String.class
        );

        try {
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(PARSING_USER_INFO_FAILS, e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException(EXTERNAL_API_TIMEOUT, e);
        }
    }
}
