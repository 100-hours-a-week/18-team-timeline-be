package com.tamnara.backend.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private boolean success;
    private String message;

    public static ErrorResponse of(boolean success, String message) {
        return ErrorResponse.builder()
                .success(success)
                .message(message)
                .build();
    }
}
