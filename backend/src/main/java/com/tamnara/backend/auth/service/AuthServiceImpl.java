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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProvider.resolveRefreshTokenFromCookie(request);

        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, AuthResponseMessage.REFRESH_TOKEN_INVALID);
        }

        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        String newAccessToken = jwtProvider.createAccessToken(user);

        Cookie newAccessCookie = new Cookie(JwtConstant.ACCESS_TOKEN, newAccessToken);
        newAccessCookie.setHttpOnly(true);
        newAccessCookie.setSecure(true);
        newAccessCookie.setPath("/");
        newAccessCookie.setMaxAge((int) JwtConstant.ACCESS_TOKEN_VALIDITY.toSeconds());

        response.addCookie(newAccessCookie);
    }
}
