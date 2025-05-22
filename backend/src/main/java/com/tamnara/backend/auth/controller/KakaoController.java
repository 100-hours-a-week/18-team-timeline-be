package com.tamnara.backend.auth.controller;

import com.tamnara.backend.auth.dto.KakaoLoginUrlResponse;
import com.tamnara.backend.auth.service.KakaoService;
import com.tamnara.backend.global.dto.WrappedDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/login-url")
    @Operation(
            summary = "카카오 로그인 요청 URL 반환",
            description = "Client id, redirect URI를 사용하여 카카오 로그인 요청 URL을 생성 및 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공. 정상 생성 및 반환 완료"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<WrappedDTO<KakaoLoginUrlResponse>> getKakaoLoginUrl() {
        String loginUrl = kakaoService.buildKakaoLoginUrl();
        return ResponseEntity.ok(
                new WrappedDTO<>(
                        true,
                        "카카오 로그인 요청 URL이 성공적으로 생성되었습니다.",
                        new KakaoLoginUrlResponse(loginUrl)
                )
        );
    }

    @GetMapping("/callback")
    @Operation(
            summary = "카카오 로그인 콜백",
            description =
                    "프론트에서 받은 인가 코드를 서버에 전달하면, 서버가 카카오에 요청하여 사용자 정보를 받아와 로그인 or 회원가입 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공. 카카오 로그인 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "401", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<WrappedDTO<Void>> kakaoCallback(@RequestParam("code") String code) {
        return kakaoService.kakaoLogin(code);
    }
}
