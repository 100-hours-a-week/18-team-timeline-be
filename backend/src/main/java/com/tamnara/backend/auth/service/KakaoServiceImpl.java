package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.auth.constant.KakaoOAuthConstant;
import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.client-id}") private String clientId;
    @Value("${kakao.redirect-uri}") private String redirectUri;

    @Override
    public String buildKakaoLoginUrl() {
        log.info("[AUTH] buildKakaoLoginUrl 요청 시작");

        String url = UriComponentsBuilder.fromUriString(KakaoOAuthConstant.KAKAO_OAUTH_AUTHORIZE)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();

        log.info("[AUTH] buildKakaoLoginUrl 완료");
        return url;
    }

    @Override
    @Transactional
    public void kakaoLogin(String code, HttpServletResponse response) {
        log.info("[AUTH] kakaoLogin 요청 시작");

        String kakaoAccessToken = kakaoApiClient.getAccessToken(code);
        log.info("[AUTH] kakaoLogin 처리 중 - 카카오 Access Token 발급 성공");

        Map<String, Object> userInfoJson = kakaoApiClient.getUserInfo(kakaoAccessToken);
        log.info("[AUTH] kakaoLogin 처리 중 - 카카오 사용자 정보 조회 성공");

        Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoJson.get("kakao_account");
        Map<String, Object> properties = (Map<String, Object>) userInfoJson.get("properties");

        String kakaoId = String.valueOf(userInfoJson.get("id"));
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) properties.get("nickname");
        log.info("[AUTH] kakaoLogin 처리 중 - 카카오 사용자 파싱 완료, kakaoId={}", kakaoId);

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId);
        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (user.getState().equals(State.DELETED)) {
                user.updateState(State.ACTIVE);
                user.resetWithdrawnAtNull();
                log.info("[AUTH] kakaoLogin 처리 중 - 탈퇴한 계정 복구, userId: {}", user.getId());
            }
        } else {
            user = optionalUser.orElseGet(() -> User.builder()
                .email(email)
                .username(nickname)
                .provider("KAKAO")
                .providerId(kakaoId)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build());
        }
        user.updateLastActiveAtNow();
        userRepository.save(user);
        log.info("[AUTH] kakaoLogin 처리 중 - 마지막 활동시간 업데이트 완료, userId: {}", user.getId());

        String accessToken = jwtProvider.createAccessToken(user);
        log.info("[AUTH] kakaoLogin 처리 중 - JWT Access Token 발급 완료, userId: {}", user.getId());

        String refreshToken = jwtProvider.createRefreshToken(user);
        jwtProvider.saveRefreshToken(user, refreshToken);
        log.info("[AUTH] kakaoLogin 처리 중 - JWT Refresh Token 발급 완료, userId: {}", user.getId());

        Cookie accessCookie = new Cookie(JwtConstant.ACCESS_TOKEN, accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge((int) JwtConstant.ACCESS_TOKEN_VALIDITY.getSeconds());
        response.addCookie(accessCookie);
        log.info("[AUTH] kakaoLogin 처리 중 - 쿠키에 JWT Access Token 저장 완료, userId: {}", user.getId());

        Cookie refreshCookie = new Cookie(JwtConstant.REFRESH_TOKEN, refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) JwtConstant.REFRESH_TOKEN_VALIDITY.getSeconds());
        response.addCookie(refreshCookie);
        log.info("[AUTH] kakaoLogin 처리 중 - 쿠키에 JWT Refresh Token 저장 완료: userId={}", user.getId());

        log.info("[AUTH] kakaoLogin 완료 - userId: {}",  user.getId());
    }
}
