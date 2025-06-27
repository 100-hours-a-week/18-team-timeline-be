package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.constant.AuthResponseMessage;
import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @InjectMocks private AuthServiceImpl authService;
    @Mock private JwtProvider jwtProvider;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    User mockUser;
    @BeforeEach
    void init() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .build();
    }

    @Test
    void 리프레시_토큰_재발급_검증() {
        // given
        String refreshToken = "validRefreshToken";
        String userId = "1";
        String newAccessToken = "newAccessToken";

        given(jwtProvider.resolveRefreshTokenFromCookie(request)).willReturn(refreshToken);
        given(jwtProvider.validateRefreshToken(refreshToken)).willReturn(true);
        given(jwtProvider.getUserIdFromToken(refreshToken)).willReturn(userId);
        given(userRepository.findById(Long.parseLong(userId))).willReturn(Optional.of(mockUser));
        given(jwtProvider.createAccessToken(mockUser)).willReturn(newAccessToken);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        // when
        authService.refreshToken(request, response);

        // then
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertEquals(JwtConstant.ACCESS_TOKEN, cookie.getName());
        assertEquals(newAccessToken, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.getSecure());
        assertEquals("/", cookie.getPath());
    }

    @Test
    void 리프레시_토큰이_없을_경우_예외_처리_검증() {
        // given
        given(jwtProvider.resolveRefreshTokenFromCookie(request)).willReturn(null); // 토큰 없음

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.refreshToken(request, response);
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals(AuthResponseMessage.REFRESH_TOKEN_INVALID, exception.getMessage());
    }
}
