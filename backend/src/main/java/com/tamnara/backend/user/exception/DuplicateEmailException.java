package com.tamnara.backend.user.exception;

import static com.tamnara.backend.user.constant.UserResponseMessage.EMAIL_UNAVAILABLE;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super(EMAIL_UNAVAILABLE);
    }
}
