package com.tamnara.backend.auth.service;

import jakarta.servlet.http.HttpServletResponse;

public interface KakaoService {
    String buildKakaoLoginUrl();
    void kakaoLogin(String code, HttpServletResponse response);
}
