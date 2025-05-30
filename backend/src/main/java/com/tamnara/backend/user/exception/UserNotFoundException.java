package com.tamnara.backend.user.exception;

import static com.tamnara.backend.global.constant.ResponseMessage.USER_NOT_FOUND;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(USER_NOT_FOUND);
    }
}

