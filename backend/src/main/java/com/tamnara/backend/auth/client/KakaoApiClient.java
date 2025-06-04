package com.tamnara.backend.auth.client;

import java.util.Map;

public interface KakaoApiClient {
    String getAccessToken(String code);
    Map<String, Object> getUserInfo(String accessToken);
}