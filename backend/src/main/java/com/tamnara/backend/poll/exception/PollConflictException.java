package com.tamnara.backend.poll.exception;

import com.tamnara.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PollConflictException extends CustomException {
    public PollConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
