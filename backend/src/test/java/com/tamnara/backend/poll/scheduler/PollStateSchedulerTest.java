package com.tamnara.backend.poll.scheduler;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.poll.util.PollTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollStateSchedulerTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollService pollService;

    @InjectMocks
    private PollStateScheduler pollStateScheduler;

    private Poll pollToDelete;
    private Poll pollToPublish;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        pollToDelete = Poll.builder()
                .id(1L)
                .minChoices(1)
                .maxChoices(2)
                .startAt(LocalDateTime.now().minusMinutes(10))
                .endAt(LocalDateTime.now().minusMinutes(1)) // 종료된 투표
                .state(PollState.PUBLISHED)
                .build();

        pollToPublish = Poll.builder()
                .id(1L)
                .minChoices(1)
                .maxChoices(2)
                .startAt(LocalDateTime.now().minusMinutes(1)) // 시작된 투표
                .endAt(LocalDateTime.now().plusMinutes(10)) // 아직 유효함
                .state(PollState.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("종료된 PUBLISHED 투표는 DELETED 상태로 변경된다")
    void updatePollStates_deletesExpiredPolls() {
        // given
        when(pollRepository.findByStateAndEndAtBefore(eq(PollState.PUBLISHED), any()))
                .thenReturn(List.of(pollToDelete));

        when(pollRepository.findByStateAndStartAtBeforeAndEndAtAfter(eq(PollState.SCHEDULED), any(), any()))
                .thenReturn(List.of());

        // when
        pollStateScheduler.updatePollStates();

        // then
        verify(pollService).deletePoll(pollToDelete);
        verify(pollService, never()).publishPoll(any());
    }

    @Test
    @DisplayName("시작 시간이 지난 SCHEDULED 투표 중 가장 빠른 투표를 PUBLISHED 상태로 변경한다")
    void updatePollStates_publishesScheduledPoll() {
        // given
        when(pollRepository.findByStateAndEndAtBefore(eq(PollState.PUBLISHED), any()))
                .thenReturn(List.of());

        when(pollRepository.findByStateAndStartAtBeforeAndEndAtAfter(eq(PollState.SCHEDULED), any(), any()))
                .thenReturn(List.of(pollToPublish));

        // when
        pollStateScheduler.updatePollStates();

        // then
        verify(pollService).publishPoll(pollToPublish);
        verify(pollService, never()).deletePoll(any());
    }

    @Test
    @DisplayName("변경할 투표가 없으면 아무 동작도 하지 않는다")
    void updatePollStates_noChanges() {
        // given
        when(pollRepository.findByStateAndEndAtBefore(eq(PollState.PUBLISHED), any()))
                .thenReturn(List.of());

        when(pollRepository.findByStateAndStartAtBeforeAndEndAtAfter(eq(PollState.SCHEDULED), any(), any()))
                .thenReturn(List.of());

        // when
        pollStateScheduler.updatePollStates();

        // then
        verify(pollService, never()).deletePoll(any());
        verify(pollService, never()).publishPoll(any());
    }
}
