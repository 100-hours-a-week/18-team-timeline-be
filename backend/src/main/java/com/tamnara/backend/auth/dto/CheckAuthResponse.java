package com.tamnara.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckAuthResponse {
    private Long userId;
    private String username;
}
