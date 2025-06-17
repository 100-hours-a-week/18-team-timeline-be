package com.tamnara.backend.user.exception;

import static com.tamnara.backend.global.constant.ResponseMessage.ACCOUNT_FORBIDDEN;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException() {
        super(ACCOUNT_FORBIDDEN);
    }
}
