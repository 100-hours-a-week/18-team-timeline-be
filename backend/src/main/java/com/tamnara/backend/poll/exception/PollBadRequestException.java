package com.tamnara.backend.poll.exception;

import com.tamnara.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PollBadRequestException extends CustomException {
    public PollBadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
