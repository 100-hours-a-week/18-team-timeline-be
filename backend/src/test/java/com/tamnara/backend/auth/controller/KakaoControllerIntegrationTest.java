package com.tamnara.backend.auth.controller;

import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.auth.config.KakaoApiClientMockConfig;
import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.tamnara.backend.auth.constant.AuthResponseMessage.KAKAO_BAD_GATEWAY;
import static com.tamnara.backend.auth.constant.AuthResponseMessage.PARSING_USER_INFO_FAILS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Import(KakaoApiClientMockConfig.class)
@ActiveProfiles("test")
class KakaoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private KakaoApiClient kakaoApiClient;
    @Autowired private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        reset(kakaoApiClient);
    }

    @Test
    @DisplayName("카카오 로그인 콜백 요청이 전체 흐름을 통해 200 OK 및 JWT 토큰을 반환한다")
    void kakaoCallback_ReturnJwtToken_success() throws Exception {
        // given
        when(kakaoApiClient.getAccessToken(anyString())).thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken")).thenReturn(Map.of(
                "id", 12345L,
                "kakao_account", Map.of("email", "test@kakao.com"),
                "properties", Map.of("nickname", "카카오유저")
        ));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "dummyCode"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString(JwtConstant.ACCESS_TOKEN + "="))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString(JwtConstant.REFRESH_TOKEN + "="))))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Secure")))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("카카오 access token 발급 실패 시 500 응답을 반환한다")
    void kakaoCallback_AccessTokenFailure_returnsBadGateway() throws Exception {
        // given
        when(kakaoApiClient.getAccessToken(anyString())).thenThrow(new RuntimeException(KAKAO_BAD_GATEWAY));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "invalidCode"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(KAKAO_BAD_GATEWAY));
    }

    @Test
    @DisplayName("카카오 사용자 정보 파싱 실패 시 500 응답을 반환한다")
    void kakaoCallback_UserInfoParsingFailure_returnsInternalServerError() throws Exception {
        // given
        when(kakaoApiClient.getAccessToken(anyString())).thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken")).thenThrow(new RuntimeException(PARSING_USER_INFO_FAILS));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "dummyCode"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(PARSING_USER_INFO_FAILS));
    }

    @Test
    @DisplayName("기존 사용자가 카카오 로그인 시 토큰 발급 및 활동시간 업데이트에 성공한다")
    void kakaoCallback_existingUser_success() throws Exception {
        // given
        User existingUser = userRepository.findByProviderAndProviderId("KAKAO", "12345")
                .orElseThrow(() -> new IllegalArgumentException(ResponseMessage.USER_NOT_FOUND));
        userRepository.save(existingUser);

        when(kakaoApiClient.getAccessToken(anyString())).thenReturn("mockAccessToken");

        when(kakaoApiClient.getUserInfo("mockAccessToken")).thenReturn(Map.of(
                "id", 12345L,
                "kakao_account", Map.of("email", "test@kakao.com"),
                "properties", Map.of("nickname", "카카오유저")
        ));

        // when & then
        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "dummyCode"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString(JwtConstant.ACCESS_TOKEN + "="))))
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE,
                        Matchers.hasItem(Matchers.containsString(JwtConstant.REFRESH_TOKEN + "="))))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, Matchers.containsString("Secure")))
                .andExpect(jsonPath("$.success").value(true));
    }
}
