package com.tamnara.backend.config;

import com.tamnara.backend.news.service.NewsService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class NewsServiceMockConfig {
    @Bean
    public NewsService newsService() {
        return Mockito.mock(NewsService.class);
    }
}
