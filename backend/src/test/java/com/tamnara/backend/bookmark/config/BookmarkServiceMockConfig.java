package com.tamnara.backend.bookmark.config;

import com.tamnara.backend.bookmark.service.BookmarkService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BookmarkServiceMockConfig {
    @Bean
    public BookmarkService bookmarkService() {
        return Mockito.mock(BookmarkService.class);
    }
}
