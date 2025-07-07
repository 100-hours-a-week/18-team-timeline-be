package com.tamnara.backend.auth.controller;

import com.tamnara.backend.auth.constant.AuthResponseMessage;
import com.tamnara.backend.auth.dto.CheckAuthResponse;
import com.tamnara.backend.auth.service.AuthService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/check")
    public ResponseEntity<WrappedDTO<CheckAuthResponse>> checkAuth(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            log.info("[AUTH] checkAuth 요청 시작");
            if (userDetails == null) {
                log.info("[AUTH] checkAuth 완료 — status={}", "로그아웃");
                return ResponseEntity.ok(new WrappedDTO<>(
                        false,
                        AuthResponseMessage.NOT_LOGGED_IN,
                        null
                ));
            }

            log.info("[AUTH] checkAuth 완료 — status={}, userId: {}", "로그인", userDetails.getUser().getId());
            return ResponseEntity.ok(new WrappedDTO<>(
                    true,
                    AuthResponseMessage.IS_LOGGED_IN,
                    new CheckAuthResponse(userDetails.getUser().getId(), userDetails.getUsername())
            ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/token/refresh")
    public ResponseEntity<WrappedDTO<Void>> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.refreshToken(request, response);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            AuthResponseMessage.REFRESH_TOKEN_SUCCESS,
                            null
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
