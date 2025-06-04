package com.tamnara.backend.global.exception;

import com.tamnara.backend.global.dto.WrappedDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tamnara.backend.global.constant.ResponseMessage.*;

@RestControllerAdvice
public class SecurityExceptionHandler {

    // 인증 실패 (토큰 없음, 만료, 위조 등)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<WrappedDTO<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new WrappedDTO<>(false, INVALID_TOKEN, null)
        );
    }

    // 인가 실패 (ROLE 부족)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<WrappedDTO<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new WrappedDTO<>(false, USER_UNAUTHORIZED, null)
        );
    }
}
