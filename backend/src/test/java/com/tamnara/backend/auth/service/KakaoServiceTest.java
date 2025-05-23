package com.tamnara.backend.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.auth.client.KakaoApiClient;
import com.tamnara.backend.auth.utils.TestUtils;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KakaoServiceTest {

    @InjectMocks
    private KakaoService kakaoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private KakaoApiClient kakaoApiClient;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // private 필드 주입
        kakaoService = new KakaoService(userRepository, jwtProvider, kakaoApiClient);
        TestUtils.setPrivateField(kakaoService, "clientId", "fake-client-id");
        TestUtils.setPrivateField(kakaoService, "redirectUri", "http://fake-localhost:8080/auth/kakao/callback");
    }

    @Test
    @DisplayName("신규 사용자가 카카오 로그인 시 회원가입 및 토큰 발급에 성공한다")
    void kakaoLogin_newUser_success() throws Exception {
        // given
        String code = "dummy_code";
        String tamnaraToken = "jwtToken";

        when(userRepository.findByProviderAndProviderId("KAKAO", "12345")).thenReturn(Optional.empty());
        when(jwtProvider.createAccessToken(any())).thenReturn(tamnaraToken);
        when(kakaoApiClient.getAccessToken(anyString())).thenReturn("mockAccessToken");
        when(kakaoApiClient.getUserInfo("mockAccessToken")).thenReturn(Map.of(
                "id", 12345L,
                "kakao_account", Map.of("email", "test@kakao.com"),
                "properties", Map.of("nickname", "카카오유저")
        ));

        // when
        ResponseEntity<WrappedDTO<Void>> response = kakaoService.kakaoLogin(code);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + tamnaraToken);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@kakao.com");
        assertThat(savedUser.getUsername()).isEqualTo("카카오유저");
        assertThat(savedUser.getProviderId()).isEqualTo("12345");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getState()).isEqualTo(State.ACTIVE);
    }
}
