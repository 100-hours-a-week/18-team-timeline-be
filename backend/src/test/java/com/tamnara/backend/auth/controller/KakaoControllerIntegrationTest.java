package com.tamnara.backend.auth.controller;

import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import org.springframework.transaction.annotation.Transactional;
import com.tamnara.backend.auth.jwt.JwtProvider;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class KakaoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;
    @MockBean private KakaoService kakaoService;

    @BeforeEach
    @Transactional
    void setUp() {
        userRepository.deleteAll();
        User user = User.builder()
                .provider("KAKAO")
                .providerId("12345")
                .email("test@kakao.com")
                .username("카카오유저")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();

        userRepository.save(user);
        userRepository.flush();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("카카오 로그인 콜백 요청이 전체 흐름을 통해 200 OK 및 JWT 토큰을 반환한다")
    void kakaoCallback_ReturnJwtToken_success() throws Exception {
        String token = jwtProvider.createAccessToken("12345", Role.USER);

        given(kakaoService.kakaoLogin(anyString()))
                .willReturn(token);

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
    @DisplayName("기존 사용자가 카카오 로그인 시 토큰 발급 및 활동시간 업데이트에 성공한다")
    void kakaoCallback_existingUser_success() throws Exception {
        String token = jwtProvider.createAccessToken("12345", Role.USER);

        given(kakaoService.kakaoLogin(anyString()))
                .willReturn(token);

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
        given(kakaoService.kakaoLogin(anyString()))
                .willThrow(new RuntimeException("KAKAO_BAD_GATEWAY"));

        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "invalidCode"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("KAKAO_BAD_GATEWAY"));
    }

    @Test
    @DisplayName("카카오 사용자 정보 파싱 실패 시 500 응답을 반환한다")
    void kakaoCallback_UserInfoParsingFailure_returnsInternalServerError() throws Exception {
        given(kakaoService.kakaoLogin(anyString()))
                .willThrow(new RuntimeException("PARSING_USER_INFO_FAILS"));

        mockMvc.perform(get("/auth/kakao/callback")
                        .param("code", "dummyCode"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("PARSING_USER_INFO_FAILS"));
    }
}
