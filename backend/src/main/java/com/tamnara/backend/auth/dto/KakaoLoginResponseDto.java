package com.tamnara.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KakaoLoginResponseDto {
    private boolean success;
    private String message;
}
