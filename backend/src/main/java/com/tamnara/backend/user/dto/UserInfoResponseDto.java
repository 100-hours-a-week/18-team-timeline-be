package com.tamnara.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserInfoResponseDto {
    private boolean success;
    private String message;
    private Data data;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Data {
        private UserData user;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserData {
        private Long userId;
        private String email;
        private String username;
    }
}
