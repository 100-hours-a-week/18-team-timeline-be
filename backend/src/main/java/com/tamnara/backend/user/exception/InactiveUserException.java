package com.tamnara.backend.user.exception;

import static com.tamnara.backend.global.constant.ResponseMessage.USER_FORBIDDEN;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException() {
        super(USER_FORBIDDEN);
    }
}
