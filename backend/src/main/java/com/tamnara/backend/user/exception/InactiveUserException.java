package com.tamnara.backend.user.exception;

import static com.tamnara.backend.user.constant.UserResponseMessage.ACCOUNT_FORBIDDEN;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException() {
        super(ACCOUNT_FORBIDDEN);
    }
}
