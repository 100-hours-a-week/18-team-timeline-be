package com.tamnara.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class KakaoUrlResponseDto {
    private boolean success;
    private String message;
    private Map<String, String> data;
}
