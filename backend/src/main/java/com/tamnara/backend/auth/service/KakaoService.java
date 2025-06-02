package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.*;
import static com.tamnara.backend.global.constant.ResponseMessage.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public String buildKakaoLoginUrl() {
        String url = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();

        log.info("카카오 로그인 URL 생성됨: {}", url);
        return url;
    }

    public ResponseEntity<WrappedDTO<Void>> kakaoLogin(String code) {
        log.info("카카오 로그인 시작: code={}", code);
        try {
            String accessToken = kakaoApiClient.getAccessToken(code);
            log.info("카카오 access token 발급 성공");

            Map<String, Object> userInfoJson = kakaoApiClient.getUserInfo(accessToken);
            log.info("카카오 사용자 정보 조회 성공");

            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoJson.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) userInfoJson.get("properties");

            String kakaoId = String.valueOf(userInfoJson.get("id"));
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) properties.get("nickname");

            log.debug("카카오 사용자 파싱 결과: kakaoId={}, email={}, nickname={}", kakaoId, email, nickname);

            Optional<User> optionalUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId);

            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get();
                log.info("기존 사용자 로그인 처리: userId={}", user.getId());
            } else {
                user = User.builder()
                        .email(email)
                        .username(nickname)
                        .provider("KAKAO")
                        .providerId(kakaoId)
                        .role(Role.USER)
                        .state(State.ACTIVE)
                        .build();
                log.info("신규 사용자 회원가입 처리 예정: email={}", email);
            }
            user.updateLastActiveAtNow();
            userRepository.save(user);
            log.info("마지막 활동시간 업데이트 완료: userId={}", user.getId());

            String tamnaraAccessToken = jwtProvider.createAccessToken(user);
            log.info("JWT 발급 완료: userId={}", user.getId());

            // System.out.println("✅ 발급된 access token: " + tamnaraAccessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamnaraAccessToken)
                    .body(new WrappedDTO<>(true, KAKAO_LOGIN_SUCCESSFUL, null));

        } catch (RestClientException e) {
            log.error("카카오 API 요청 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new WrappedDTO<>(false, KAKAO_BAD_GATEWAY + e.getMessage(), null));
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 예외 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WrappedDTO<>(false, INTERNAL_SERVER_ERROR + e.getMessage(), null));
        }
    }
}
