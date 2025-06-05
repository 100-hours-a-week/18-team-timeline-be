package com.tamnara.backend.user.dto;

import java.time.LocalDateTime;

public record UserWithdrawInfo(
        Long userId,
        LocalDateTime withdrawnAt
) {}
