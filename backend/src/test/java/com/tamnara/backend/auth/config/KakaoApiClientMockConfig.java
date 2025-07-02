package com.tamnara.backend.auth.config;

import com.tamnara.backend.auth.client.KakaoApiClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class KakaoApiClientMockConfig {
    @Bean
    public KakaoApiClient kakaoApiClient() {
        return Mockito.mock(KakaoApiClient.class);
    }
}
