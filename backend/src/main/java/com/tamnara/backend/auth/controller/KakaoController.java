package com.tamnara.backend.auth.controller;

import com.tamnara.backend.auth.dto.KakaoLoginResponseDto;
import com.tamnara.backend.auth.dto.KakaoUrlResponseDto;
import com.tamnara.backend.auth.service.KakaoService;
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
    public ResponseEntity<KakaoUrlResponseDto> getKakaoLoginUrl() {
        String loginUrl = kakaoService.buildKakaoLoginUrl();
        return ResponseEntity.ok(
                new KakaoUrlResponseDto(
                        true,
                        "카카오 로그인 요청 URL이 성공적으로 생성되었습니다.",
                        Map.of("loginUrl", loginUrl)
                )
        );
    }

    @GetMapping("/callback")
    public ResponseEntity<KakaoLoginResponseDto> kakaoCallback(@RequestParam("code") String code) {
        return kakaoService.kakaoLogin(code);
    }
}
