package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.*;
import static com.tamnara.backend.global.constant.ResponseMessage.INTERNAL_SERVER_ERROR;

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
        return UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .toUriString();
    }

    public ResponseEntity<WrappedDTO<Void>> kakaoLogin(String code) {
        try {
            // access token 요청
            String accessToken = kakaoApiClient.getAccessToken(code);

            // 사용자 정보 요청
            Map<String, Object> userInfoJson = kakaoApiClient.getUserInfo(accessToken);

            // 사용자 정보 파싱
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoJson.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) userInfoJson.get("properties");

            String kakaoId = String.valueOf(userInfoJson.get("id"));
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) properties.get("nickname");

            // 이미 카카오 로그인으로 가입된 사용자인지 확인
            Optional<User> optionalUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId);

            User user;

            if (optionalUser.isPresent()) {
                // 기존 사용자 → 로그인 처리
                user = optionalUser.get();
            } else {
                // 신규 사용자 → 회원가입 처리
                user = User.builder()
                        .email(email)
                        .username(nickname)
                        .provider("KAKAO")
                        .providerId(kakaoId)
                        .role(Role.USER)         // 기본 권한
                        .state(State.ACTIVE)     // 기본 상태
                        .build();
            }
            user.updateLastActiveAtNow();
            userRepository.save(user);

            String tamnaraAccessToken = jwtProvider.createAccessToken(user);

            // 콘솔에 access token 출력 (로컬 디버깅용)
//            System.out.println("✅ 발급된 access token: " + tamnaraAccessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamnaraAccessToken)
                    .body(new WrappedDTO<>(true, KAKAO_LOGIN_SUCCESSFUL, null));

        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new WrappedDTO<>(false, KAKAO_BAD_GATEWAY + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WrappedDTO<>(false, INTERNAL_SERVER_ERROR + e.getMessage(), null));
        }
    }
}
