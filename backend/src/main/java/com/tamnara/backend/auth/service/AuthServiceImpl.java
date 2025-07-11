package com.tamnara.backend.auth.service;

import com.tamnara.backend.auth.constant.AuthResponseMessage;
import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("[AUTH] refreshToken 시작");

        String refreshToken = jwtProvider.resolveRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            log.warn("[AUTH] refreshToken 실패 — reason:{}", "토큰 없음");
            throw new CustomException(HttpStatus.UNAUTHORIZED, AuthResponseMessage.REFRESH_TOKEN_INVALID);
        } else if (!jwtProvider.validateRefreshToken(refreshToken)) {
            log.warn("[AUTH] refreshToken 실패 — reason:{}", "유효하지 않은 토큰");
            throw new CustomException(HttpStatus.UNAUTHORIZED, AuthResponseMessage.REFRESH_TOKEN_INVALID);
        }

        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[AUTH] refreshToken 처리 중 — 회원 조회 완료, userId:{}", userId);

        String newAccessToken = jwtProvider.createAccessToken(user);
        log.info("[AUTH] refreshToken 처리 중 - 새로운 Access Token 발급, userId:{}", userId);

        Cookie newAccessCookie = new Cookie(JwtConstant.ACCESS_TOKEN, newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setSecure(true);
        newAccessCookie.setPath("/");
        newAccessCookie.setMaxAge((int) JwtConstant.ACCESS_TOKEN_VALIDITY.toSeconds());
        response.addCookie(newAccessCookie);
        log.info("[AUTH] refreshToken 처리 중 - 새로운 Access Token 쿠키 저장, userId:{}", userId);

        log.info("[AUTH] refreshToken() 완료 - status:{}", "성공");
    }
}
