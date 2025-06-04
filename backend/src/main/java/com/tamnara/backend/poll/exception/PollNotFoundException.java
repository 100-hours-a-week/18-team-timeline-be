package com.tamnara.backend.poll.exception;

import com.tamnara.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PollNotFoundException extends CustomException {
    public PollNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
