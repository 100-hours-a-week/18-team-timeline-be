package com.tamnara.backend.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponseDto {
    private boolean success;
    private String message;
    private UserData data;

    @Getter
    @Builder
    public static class UserData {
        private Long userId;
    }
}
