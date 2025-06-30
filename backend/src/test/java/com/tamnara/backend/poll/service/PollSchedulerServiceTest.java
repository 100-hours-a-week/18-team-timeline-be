package com.tamnara.backend.poll.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PollSchedulerServiceTest {

    @Mock private PollService pollService;

    @InjectMocks private PollSchedulerServiceImpl pollSchedulerService;

    @Test
    @DisplayName("투표 상태 전환 자동화 스케줄링 메서드 처리 성공")
    void updatePollStates() {
        // given

        // when
        pollSchedulerService.updatePollStates();

        // then
        verify(pollService, times(1)).updatePollStates();
    }
}
