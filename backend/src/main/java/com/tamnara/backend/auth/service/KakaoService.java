package com.tamnara.backend.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

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
            RestTemplate restTemplate = new RestTemplate();

            // 1. 토큰 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 2. 요청 파라미터 설정
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

            // 3. access_token 요청
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            // 4. access_token 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> tokenJson = objectMapper.readValue(tokenResponse.getBody(), Map.class);
            String accessToken = (String) tokenJson.get("access_token");

            // 5. 사용자 정보 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.add("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    String.class
            );

            // 6. 사용자 정보 파싱
            Map<String, Object> userInfoJson = objectMapper.readValue(userInfoResponse.getBody(), Map.class);
            Map<String, Object> kakaoAccount = (Map<String, Object>) userInfoJson.get("kakao_account");
            Map<String, Object> properties = (Map<String, Object>) userInfoJson.get("properties");

            String kakaoId = String.valueOf(userInfoJson.get("id"));
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) properties.get("nickname");

            // 7. 이미 카카오 로그인으로 가입된 사용자인지 확인
            Optional<User> optionalUser = userRepository.findByProviderAndProviderId("KAKAO", kakaoId);

            User user;

            if (optionalUser.isPresent()) {
                // 기존 사용자 → 로그인 처리
                user = optionalUser.get();
                user.updateLastActiveAtNow();
                userRepository.save(user);
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

                user.updateLastActiveAtNow();
                userRepository.save(user);
            }

            String tamnaraAccessToken = jwtProvider.createAccessToken(user);

            // 콘솔에 access token 출력 (로컬 디버깅용)
//            System.out.println("✅ 발급된 access token: " + tamnaraAccessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tamnaraAccessToken)
                    .body(new WrappedDTO<>(true, "카카오 로그인이 성공적으로 완료되었습니다.", null));

        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WrappedDTO<>(false, "❌ JSON 파싱 오류: " + e.getMessage(), null));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new WrappedDTO<>(false, "❌ 카카오 서버 요청 실패: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WrappedDTO<>(false, "❌ 예상치 못한 오류 발생: " + e.getMessage(), null));
        }
    }
}
