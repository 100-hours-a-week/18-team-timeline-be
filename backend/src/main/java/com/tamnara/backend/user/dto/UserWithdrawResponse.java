package com.tamnara.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserWithdrawResponse {
    private Long userId;
    private LocalDateTime withdrawnAt;
}
