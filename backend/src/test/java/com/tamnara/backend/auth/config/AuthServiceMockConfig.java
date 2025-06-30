package com.tamnara.backend.auth.config;

import com.tamnara.backend.auth.service.AuthService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AuthServiceMockConfig {
    @Bean
    public AuthService authService() {
        return Mockito.mock(AuthService.class);
    }
}
