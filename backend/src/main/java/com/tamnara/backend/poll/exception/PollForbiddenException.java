package com.tamnara.backend.poll.exception;

import com.tamnara.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PollForbiddenException extends CustomException {
    public PollForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
