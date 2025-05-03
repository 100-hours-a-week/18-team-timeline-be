package com.tamnara.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoDto {
    private Long userId;
    private String email;
    private String username;
}
