package com.tamnara.backend.poll.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.util.PollCreateRequestTestBuilder;
import com.tamnara.backend.poll.util.PollOptionTestBuilder;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.tamnara.backend.poll.constant.PollResponseMessage.MIN_CHOICES_EXCEED_MAX;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_FOUND;
import static com.tamnara.backend.poll.constant.PollResponseMessage.START_DATE_LATER_THAN_END_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollServiceImplTest {

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private UserRepository userRepository;
    @Mock private PollRepository pollRepository;
    @Mock private PollOptionRepository pollOptionRepository;
    @Mock private VoteRepository voteRepository;

    @InjectMocks private PollServiceImpl pollServiceImpl;

    private Poll poll;
    private PollOption option;
    private Poll pollToDelete;
    private Poll pollToPublish;

    @BeforeEach
    void setUp() {
        poll = PollTestBuilder.defaultPoll();
        ReflectionTestUtils.setField(poll, "id", 1L);
        option = PollOptionTestBuilder.defaultOption(poll);

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
    @DisplayName("createPoll 실행 성공")
    void createPoll_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 1, 2, now.minusDays(1), now.plusDays(1),
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        when(pollRepository.save(any(Poll.class))).thenReturn(poll);
        when(pollOptionRepository.saveAll(Mockito.anyList())).thenReturn(Arrays.asList(option));

        // when
        Long pollId = pollServiceImpl.createPoll(request);

        // then
        assertThat(pollId).isEqualTo(poll.getId());
        Mockito.verify(pollRepository, times(1)).save(any(Poll.class));
        Mockito.verify(pollOptionRepository, times(1)).saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("createPoll 실행에서 minChoices가 maxChoices보다 큰 경우 400 에러 발생")
    void createPoll_throwsException_whenMinChoicesExceedsMaxChoices() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 5, 3, now.minusDays(1), now.plusDays(1),
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        // when & then
        assertThatThrownBy(() -> pollServiceImpl.createPoll(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MIN_CHOICES_EXCEED_MAX);
    }

    @Test
    @DisplayName("createPoll 실행에서 start_at이 end_at보다 나중인 경우 400 에러 발생")
    void createPoll_throwsException_whenStartDateIsAfterEndDate() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 1, 2, now.plusDays(1), now.minusDays(1),
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        // when & then
        assertThatThrownBy(() -> pollServiceImpl.createPoll(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(START_DATE_LATER_THAN_END_DATE);
    }

    @Test
    @DisplayName("getLatestPublishedPoll 실행에서 정상적으로 Poll 반환")
    void getLatestPublishedPoll_returnsPoll_whenPollExists() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.ofNullable(poll));

        // when
        PollInfoResponse response = pollServiceImpl.getLatestPublishedPoll(1L);

        // then
        assertEquals(response.getPoll().getId(), poll.getId());
    }

    @Test
    @DisplayName("getLatestPublishedPoll 실행에서 PollId가 존재하지 않는 경우 예외 발생")
    void getLatestPublishedPoll_returnsNull_whenPollDoesNotExist() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.getLatestPublishedPoll(poll.getId());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(POLL_NOT_FOUND, exception.getReason());
    }

    @Test
    @DisplayName("schedulePoll을 통해 state 변경 성공")
    void schedulePoll_changesStateToScheduled() {
        // given
        when(pollRepository.findById(anyLong())).thenReturn(Optional.ofNullable(poll));

        // when
        pollServiceImpl.schedulePoll(poll.getId());

        // then
        assertEquals(PollState.SCHEDULED, poll.getState());
        verify(pollRepository, times(1)).save(poll);
    }

    @Test
    @DisplayName("SCHEDULED 투표 1건을 PUBLISHED 상태로 변경, 기존 PUBLISHED 투표를 DELETED 상태로 변경")
    void updatePollStates_deletesExpiredPolls() {
        // given
        when(pollRepository.findLatesPollByScheduledPoll()).thenReturn(Optional.ofNullable(pollToPublish));
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.ofNullable(pollToDelete));

        // when
        pollServiceImpl.updatePollStates();

        // then
        verify(pollRepository, times(2)).save(any(Poll.class));

    }

    @Test
    @DisplayName("투표 공개 대상이 없으면 예외 처리")
    void updatePollStates_publishesScheduledPoll() {
        // given
        when(pollRepository.findLatesPollByScheduledPoll()).thenReturn(Optional.empty());

        // when
        pollServiceImpl.updatePollStates();

        // then
        verify(pollRepository, never()).save(any(Poll.class));
    }

    @Test
    @DisplayName("투표 삭제 대상이 없으면 예외 처리")
    void updatePollStates_noChanges() {
        // given
        when(pollRepository.findLatesPollByScheduledPoll()).thenReturn(Optional.ofNullable(pollToPublish));
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.empty());

        // when
        pollServiceImpl.updatePollStates();

        // then
        verify(pollRepository, times(1)).save(any(Poll.class));
    }

    @Test
    @DisplayName("투표 생성 시 전체 알림 발행 검증")
    void createPoll_createAlarm_success() {
        // given
        String pollTitle = "Test Poll";
        LocalDateTime now = LocalDateTime.now();
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                pollTitle, 1, 2, now.minusDays(1), now.plusDays(1),
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        when(pollRepository.save(any(Poll.class))).thenReturn(poll);
        when(pollOptionRepository.saveAll(Mockito.anyList())).thenReturn(Arrays.asList(option));

        // when
        pollServiceImpl.createPoll(request);

        // then
        Mockito.verify(userRepository, times(1)).findAll();
        Mockito.verify(pollOptionRepository, times(1)).saveAll(Mockito.anyList());

        ArgumentCaptor<AlarmEvent> captor = ArgumentCaptor.forClass(AlarmEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AlarmEvent event = captor.getValue();
        assertEquals(AlarmMessage.POLL_START_TITLE, event.getTitle());
        assertEquals(String.format(AlarmMessage.POLL_START_CONTENT, pollTitle), event.getContent());
        assertEquals(AlarmType.POLLS, event.getTargetType());
        assertNull(event.getTargetId());
    }
}
