package com.tamnara.backend.auth.controller;

import com.tamnara.backend.auth.constant.AuthResponseMessage;
import com.tamnara.backend.auth.dto.KakaoLoginUrlResponse;
import com.tamnara.backend.auth.service.KakaoServiceImpl;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.KAKAO_BAD_GATEWAY;
import static com.tamnara.backend.auth.constant.AuthResponseMessage.KAKAO_LOGIN_URL_GENERATGED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoController {

    private final KakaoServiceImpl kakaoService;

    @GetMapping("/login-url")
    @Operation(
            summary = "카카오 로그인 요청 URL 반환",
            description = "Client id, redirect URI를 사용하여 카카오 로그인 요청 URL을 생성 및 반환한다."
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
                        KAKAO_LOGIN_URL_GENERATGED,
                        new KakaoLoginUrlResponse(loginUrl)
                )
        );
    }

    @GetMapping("/callback")
    @Operation(
            summary = "카카오 로그인 콜백",
            description = "클라이언트에서 받은 인가 코드를 카카오에 요청하고 사용자 정보를 받아와 로그인/회원가입 처리한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공. 카카오 로그인 완료"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "401", description = "잘못된 인가 코드"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<WrappedDTO<Void>> kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        try {
        kakaoService.kakaoLogin(code, response);

        return ResponseEntity.ok().body(
                new WrappedDTO<>(
                        true,
                        AuthResponseMessage.KAKAO_LOGIN_SUCCESSFUL,
                        null
        ));

        } catch (RestClientException e) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, KAKAO_BAD_GATEWAY);
        } catch (Exception e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
