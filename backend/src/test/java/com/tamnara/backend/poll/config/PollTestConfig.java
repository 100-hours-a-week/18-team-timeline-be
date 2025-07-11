package com.tamnara.backend.poll.config;

import com.tamnara.backend.poll.service.PollService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PollTestConfig {

    @Bean
    public PollService pollService() {
        return Mockito.mock(PollService.class);
    }

}
