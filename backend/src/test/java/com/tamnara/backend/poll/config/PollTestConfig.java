package com.tamnara.backend.poll.config;

import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.poll.service.VoteService;
import com.tamnara.backend.poll.service.VoteStatisticsService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class PollTestConfig {

    @Bean
    public PollService pollService() {
        return Mockito.mock(PollService.class);
    }

    @Bean
    public VoteService voteService() {
        return Mockito.mock(VoteService.class);
    }

    @Bean
    public VoteStatisticsService voteStatisticsService() {
        return Mockito.mock(VoteStatisticsService.class);
    }
}
