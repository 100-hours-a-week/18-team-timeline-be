package com.tamnara.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserEmailCheckResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Data {
        private boolean available;
    }
}
