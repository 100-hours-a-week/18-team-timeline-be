package com.tamnara.backend.user.controller;

import com.tamnara.backend.user.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/login-url")
    public ResponseEntity<?> getKakaoLoginUrl() {
        String loginUrl = kakaoService.buildKakaoLoginUrl();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "카카오 로그인 요청 URL이 성공적으로 생성되었습니다.",
                "data", Map.of("loginUrl", loginUrl)
        ));
    }

    @GetMapping("/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code) {
        return kakaoService.kakaoLogin(code);
    }
}
