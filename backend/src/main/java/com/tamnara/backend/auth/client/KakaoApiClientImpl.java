package com.tamnara.backend.auth.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.*;

@Service
@RequiredArgsConstructor
public class KakaoApiClientImpl implements KakaoApiClient {

    @Value("${kakao.client-id}") private String clientId;
    @Value("${kakao.redirect-uri}") private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<?> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                String.class
        );

        try {
            Map<String, Object> body = objectMapper.readValue(response.getBody(), Map.class);
            return (String) body.get("access_token");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(PARSING_ACCESS_TOKEN_FAILS, e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException(EXTERNAL_API_TIMEOUT, e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
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
