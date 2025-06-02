package com.tamnara.backend.comment.config;

import com.tamnara.backend.comment.service.CommentService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CommentServiceMockConfig {
    @Bean
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }
}
