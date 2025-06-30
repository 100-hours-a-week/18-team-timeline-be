package com.tamnara.backend.global.constant;


import java.time.Duration;

public final class JwtConstant {
    private JwtConstant() {}

    public static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(60);
    public static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(7);
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
}
