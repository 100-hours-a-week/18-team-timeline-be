package com.tamnara.backend.global.exception;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.response.ErrorResponse;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static com.tamnara.backend.global.constant.ResponseMessage.BAD_REQUEST;
import static com.tamnara.backend.global.constant.ResponseMessage.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<WrappedDTO<Void>> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getStatus()).body(
                new WrappedDTO<>(
                        false,
                        e.getMessage(),
                        null
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<WrappedDTO<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(
                new WrappedDTO<>(false, BAD_REQUEST, null)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<WrappedDTO<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                new WrappedDTO<>(false, BAD_REQUEST, null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WrappedDTO<Void>> handleOtherExceptions(Exception ex) {
        return ResponseEntity.internalServerError().body(
                new WrappedDTO<>(false, INTERNAL_SERVER_ERROR, null)
        );
    }
}
