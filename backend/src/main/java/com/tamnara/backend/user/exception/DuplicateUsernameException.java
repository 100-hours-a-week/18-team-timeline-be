package com.tamnara.backend.user.exception;

import static com.tamnara.backend.user.constant.UserResponseMessage.NICKNAME_UNAVAILABLE;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() {
        super(NICKNAME_UNAVAILABLE);
    }
}