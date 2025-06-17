package com.tamnara.backend.poll.scheduler;

import com.tamnara.backend.poll.service.VoteStatisticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VoteStatisticsSchedulerTest {

    @Mock
    private VoteStatisticsServiceImpl voteStatisticsService;

    @InjectMocks
    private VoteStatisticsScheduler voteStatisticsScheduler;

    @Test
    @DisplayName("generateVoteStatistics는 voteStatisticsService.generateAllStatistics()를 호출한다")
    void generateVoteStatistics_callsServiceMethod() {
        // when
        voteStatisticsScheduler.generateVoteStatistics();

        // then
        verify(voteStatisticsService, times(1)).generateAllStatistics();
    }
}
