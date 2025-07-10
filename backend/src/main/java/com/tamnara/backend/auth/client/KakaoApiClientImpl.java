package com.tamnara.backend.auth.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.auth.constant.AuthResponseMessage;
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
import org.springframework.web.client.*;

import java.util.Map;

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
        String partialCode = code.substring(0, 4);
        log.info("[AUTH] getAccessToken 시작 - code:{}", partialCode);

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

            log.info("[AUTH] getAccessToken 완료 - code:{}", partialCode);
            return (String) body.get("access_token");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[AUTH] getAccessToken 카카오 응답 오류 - code:{}", partialCode);
            throw new RuntimeException("카카오 요청 실패", e);
        } catch (ResourceAccessException e) {
            log.error("[AUTH] getAccessToken 카카오 연결 오류 - code:{}", partialCode);
            throw new RuntimeException(AuthResponseMessage.EXTERNAL_API_TIMEOUT, e);
        } catch (JsonProcessingException e) {
            log.error("[AUTH] getAccessToken 카카오 토큰 파싱 실패 - code:{}", partialCode);
            throw new RuntimeException(AuthResponseMessage.PARSING_ACCESS_TOKEN_FAILS, e);
        } catch (Exception e) {
            log.error("[AUTH] getAccessToken 알 수 없는 예외 발생 - code:{}", partialCode);
            throw new RuntimeException("알 수 없는 오류", e);
        }
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        String partialToken = accessToken.substring(0, 4);
        log.info("[AUTH] getUserInfo 시작 - accessToken: {}", partialToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    KakaoOAuthConstant.KAKAO_OAUTH_USER_INFO,
                    HttpMethod.GET,
                    request,
                    String.class
            );
            log.info("[AUTH] getUserInfo 카카오 응답 반환 - accessToken: {}", partialToken);

            Map<String, Object> userInfo = objectMapper.readValue(response.getBody(), Map.class);
            log.info("[AUTH] getUserInfo 완료 - accessToken: {}", partialToken);

            return userInfo;
        } catch (JsonProcessingException e) {
            log.error("[AUTH] getUserInfo Json 처리 실패 - accessToken: {}", partialToken);
            throw new RuntimeException(AuthResponseMessage.PARSING_USER_INFO_FAILS, e);
        } catch (ResourceAccessException e) {
            log.error("[AUTH] getUserInfo 리소스 접근 실패 - accessToken: {}", partialToken);
            throw new RuntimeException(AuthResponseMessage.EXTERNAL_API_TIMEOUT, e);
        } catch (RestClientException e) {
            log.error("[AUTH] getUserInfo 외부 API 호출 실패 - accessToken: {}", partialToken);
            throw new RuntimeException(AuthResponseMessage.EXTERNAL_API_CALL_FAIL, e);
        }
    }
}
