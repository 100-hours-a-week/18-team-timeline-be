package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.global.util.TestUtil;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.tamnara.backend.global.util.CreateUserUtil.createActiveUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KakaoServiceTest {

    @InjectMocks private KakaoServiceImpl kakaoService;

    @Mock private UserRepository userRepository;
    @Mock private JwtProvider jwtProvider;
    @Mock private KakaoApiClient kakaoApiClient;
    @Mock MockHttpServletResponse mockResponse;

    @Captor private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kakaoService = new KakaoServiceImpl(userRepository, jwtProvider, kakaoApiClient);
        mockResponse = new MockHttpServletResponse();

        TestUtil.setPrivateField(kakaoService, "clientId", "fake-client-id");
        TestUtil.setPrivateField(kakaoService, "redirectUri", "http://fake-localhost:8080/auth/kakao/callback");
    }

    @Test
    @DisplayName("신규 사용자가 카카오 로그인 시 회원가입 및 토큰 발급에 성공한다")
    void kakaoLogin_newUser_success() throws Exception {
        // given
        String code = "dummy_code";
        String tamnaraToken = "jwtToken";

        when(userRepository.findByProviderAndProviderId("KAKAO", "12345"))
                .thenReturn(Optional.empty());
        when(jwtProvider.createAccessToken(any()))
                .thenReturn(tamnaraToken);
        when(kakaoApiClient.getAccessToken(anyString()))
                .thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken"))
                .thenReturn(Map.of(
                        "id", 12345L,
                        "kakao_account", Map.of("email", "test@kakao.com"),
                        "properties", Map.of("nickname", "카카오유저")
                ));

        // when
        kakaoService.kakaoLogin(code, mockResponse);

        // then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@kakao.com");
        assertThat(savedUser.getUsername()).isEqualTo("카카오유저");
        assertThat(savedUser.getProviderId()).isEqualTo("12345");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getState()).isEqualTo(State.ACTIVE);

        Cookie accessTokenCookie = mockResponse.getCookie("accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo(tamnaraToken);
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getSecure()).isTrue();

    }

    @Test
    @DisplayName("기존 사용자가 카카오 로그인 시 토큰 발급에 성공한다")
    void kakaoLogin_existingUser_success() throws Exception {
        // given
        String code = "dummy_code";
        String tamnaraToken = "jwtToken";

        User existingUser = createActiveUser("test@kakao.com", "카카오유저", "KAKAO", "12345");

        when(userRepository.findByProviderAndProviderId("KAKAO", "12345"))
                .thenReturn(Optional.of(existingUser));
        when(jwtProvider.createAccessToken(existingUser))
                .thenReturn(tamnaraToken);
        when(kakaoApiClient.getAccessToken(code))
                .thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken"))
                .thenReturn(Map.of(
                        "id", 12345L,
                        "kakao_account", Map.of("email", "test@kakao.com"),
                        "properties", Map.of("nickname", "카카오유저")
                ));

        // when
        kakaoService.kakaoLogin(code, mockResponse);

        // then
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@kakao.com");
        assertThat(savedUser.getUsername()).isEqualTo("카카오유저");
        assertThat(savedUser.getProviderId()).isEqualTo("12345");

        Cookie accessTokenCookie = mockResponse.getCookie("accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo(tamnaraToken);
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getSecure()).isTrue();
        assertThat(accessTokenCookie.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 마지막 활동 시간이 갱신된다")
    void kakaoLogin_existingUser_shouldUpdateLastActiveTime() {
        // given
        String code = "dummy_code";
        String tamnaraToken = "jwtToken";

        User existingUser = spy(createActiveUser("test@kakao.com", "카카오유저", "KAKAO", "12345"));
        LocalDateTime originalLastActiveAt = existingUser.getLastActiveAt();

        when(kakaoApiClient.getAccessToken(code))
                .thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken"))
                .thenReturn(Map.of(
                        "id", 12345L,
                        "kakao_account", Map.of("email", "test@kakao.com"),
                        "properties", Map.of("nickname", "카카오유저")
                ));

        when(userRepository.findByProviderAndProviderId("KAKAO", "12345"))
                .thenReturn(Optional.of(existingUser));
        when(jwtProvider.createAccessToken(existingUser))
                .thenReturn(tamnaraToken);

        // when
        kakaoService.kakaoLogin(code, mockResponse);

        // then
        // Verify header
        Cookie accessTokenCookie = mockResponse.getCookie("accessToken");
        assertThat(accessTokenCookie).isNotNull();
        assertThat(accessTokenCookie.getValue()).isEqualTo(tamnaraToken);
        assertThat(accessTokenCookie.isHttpOnly()).isTrue();
        assertThat(accessTokenCookie.getSecure()).isTrue();

        // Verify implementations
        verify(kakaoApiClient).getAccessToken(code);
        verify(kakaoApiClient).getUserInfo("mockAccessToken");

        verify(existingUser).updateLastActiveAtNow();
        assertThat(existingUser.getLastActiveAt())
                .isAfter(originalLastActiveAt);

        verify(userRepository).save(existingUser);
        verify(jwtProvider).createAccessToken(existingUser);
    }
}
