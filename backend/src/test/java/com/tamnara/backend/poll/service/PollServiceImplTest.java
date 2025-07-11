package com.tamnara.backend.poll.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.poll.constant.PollResponseMessage;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.domain.VoteStatistics;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.repository.VoteStatisticsRepository;
import com.tamnara.backend.poll.util.PollCreateRequestTestBuilder;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.user.domain.User;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    @Mock private VoteStatisticsRepository voteStatisticsRepository;

    @InjectMocks private PollServiceImpl pollServiceImpl;

    private User user;
    private Poll poll;
    private Poll pollToDelete;
    private Poll pollToPublish;
    private PollOption option1;
    private PollOption option2;
    private PollOption option3;
    private VoteStatistics optionStats1;
    private VoteStatistics optionStats2;
    private VoteStatistics optionStats3;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .build();

        poll = Poll.builder()
                .id(1L)
                .title("투표 제목")
                .minChoices(1)
                .maxChoices(2)
                .startAt(LocalDateTime.now().minusDays(3))
                .endAt(LocalDateTime.now().plusDays(3))
                .state(PollState.PUBLISHED)
                .options(new ArrayList<>())
                .build();

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

        option1 = PollOption.builder()
                .id(101L)
                .title("Option 1")
                .poll(poll)
                .build();

        option2 = PollOption.builder()
                .id(102L)
                .title("Option 2")
                .poll(poll)
                .build();

        option3 = PollOption.builder()
                .id(103L)
                .title("Option 3")
                .poll(poll)
                .build();

        poll.getOptions().add(option1);
        poll.getOptions().add(option2);
        poll.getOptions().add(option3);

        optionStats1 = VoteStatistics.builder()
                .id(11L)
                .count(10L)
                .createdAt(LocalDateTime.now())
                .poll(poll)
                .option(option1)
                .build();

        optionStats2 = VoteStatistics.builder()
                .id(12L)
                .count(9L)
                .createdAt(LocalDateTime.now())
                .poll(poll)
                .option(option2)
                .build();

        optionStats3 = VoteStatistics.builder()
                .id(11L)
                .count(8L)
                .createdAt(LocalDateTime.now())
                .poll(poll)
                .option(option3)
                .build();
    }

    @Test
    @DisplayName("createPoll 실행 성공")
    void createPoll_success() {
        // given
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 1, 2,
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        when(pollRepository.save(any(Poll.class))).thenReturn(poll);
        when(pollOptionRepository.saveAll(Mockito.anyList())).thenReturn(List.of(option1));

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
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 5, 3,
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        // when & then
        assertThatThrownBy(() -> pollServiceImpl.createPoll(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(PollResponseMessage.MIN_CHOICES_EXCEED_MAX);
    }

    @Test
    @DisplayName("getLatestPublishedPoll 실행에서 정상적으로 Poll 반환")
    void getLatestPublishedPoll_returnsPoll_whenPollExists() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.ofNullable(poll));

        // when
        PollInfoResponse response = pollServiceImpl.getLatestPublishedPoll(user.getId());

        // then
        assertEquals(response.getPoll().getId(), poll.getId());
    }

    @Test
    @DisplayName("getLatestPublishedPoll 실행에서 PollId가 존재하지 않는 경우 예외 발생")
    void getLatestPublishedPoll_returnsNull_whenPollDoesNotExist() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> pollServiceImpl.getLatestPublishedPoll(poll.getId())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_NOT_FOUND, exception.getReason());
    }

    @Test
    @DisplayName("scheduled Poll을 통해 state 변경 성공")
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
    @DisplayName("투표 상태 전환 시 전체 알림 발행 검증")
    void updatePollStates_createAlarm_success() {
        // given
        when(pollRepository.findLatesPollByScheduledPoll()).thenReturn(Optional.ofNullable(pollToPublish));
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.ofNullable(pollToDelete));

        // when
        pollServiceImpl.updatePollStates();

        // then
        verify(userRepository, times(1)).findAll();

        ArgumentCaptor<AlarmEvent> captor = ArgumentCaptor.forClass(AlarmEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AlarmEvent event = captor.getValue();
        assertEquals(AlarmMessage.POLL_START_TITLE, event.getTitle());
        assertEquals(String.format(AlarmMessage.POLL_START_CONTENT, pollToPublish.getTitle()), event.getContent());
        assertEquals(AlarmType.POLLS, event.getTargetType());
        assertNull(event.getTargetId());
    }

    @Test
    @DisplayName("투표 기록 저장에 성공")
    void vote_success() {
        // given
        VoteRequest voteRequest = new VoteRequest(List.of(option1.getId(), option2.getId()));

        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(any())).thenReturn(List.of(option1, option2));
        when(voteRepository.hasVotedLatestPublishedPoll(user.getId())).thenReturn(false);

        // when
        pollServiceImpl.vote(user, voteRequest);

        // then
        verify(voteRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("투표 대상 투표가 존재하지 않는 경우 404 예외를 반환한다")
    void vote_pollNotFound() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.vote(user, new VoteRequest());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_NOT_FOUND, exception.getReason());
    }

    @Test
    @DisplayName("투표가 PUBLISHED 상태가 아닌 경우 403 예외를 반환한다")
    void vote_pollNotPublished() {
        // given
        poll.changeState(PollState.SCHEDULED);
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));

        // when & then
        assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.vote(user, new VoteRequest());
        });
    }

    @Test
    @DisplayName("투표 기간이 아닌 경우 403 예외를 반환한다")
    void vote_outOfPeriod() {
        // given
        LocalDateTime now = LocalDateTime.now();
        poll = PollTestBuilder.build("test", 1, 2, now.plusDays(1), now.plusDays(3), PollState.PUBLISHED);
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.vote(user, new VoteRequest());
        });

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_NOT_IN_VOTING_PERIOD, exception.getReason());
    }

    @Test
    @DisplayName("이미 투표한 경우 409 예외를 반환한다")
    void vote_alreadyVoted() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L))).thenReturn(List.of(option1));
        when(voteRepository.hasVotedLatestPublishedPoll(user.getId())).thenReturn(true);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.vote(user, new VoteRequest(List.of(101L)));
        });

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_ALREADY_VOTED, exception.getReason());
    }

    @Test
    @DisplayName("선택된 옵션 수가 범위를 벗어나는 경우 400 예외를 반환한다")
    void vote_optionCountInvalid() {
        // given
        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L, 102L, 103L)))
                .thenReturn(List.of(option1, option2, option3));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.vote(user, new VoteRequest(List.of(101L, 102L, 103L)));
        });

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_INVALID_SELECTION_COUNT, exception.getReason());
    }

    @Test
    @DisplayName("투표 결과 통계 조회에 성공한다.")
    void voteStatistics_success() {
        // given
        List<VoteStatistics> voteStatisticsList = List.of(optionStats1, optionStats2,  optionStats3);
        when(pollRepository.findById(poll.getId())).thenReturn(Optional.of(poll));
        when(voteStatisticsRepository.findByPollId(poll.getId())).thenReturn(voteStatisticsList);

        // when
        pollServiceImpl.getVoteStatistics(poll.getId());

        // then
        verify(voteStatisticsRepository, times(1)).findByPollId(any());
    }

    @Test
    @DisplayName("투표 결과 통계 조회 시 투표가 존재하지 않으면 404 예외를 반환한다.")
    void voteStatistics_pollNotFound() {
        // given
        List<VoteStatistics> voteStatisticsList = List.of(optionStats1, optionStats2,  optionStats3);
        when(pollRepository.findById(poll.getId())).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            pollServiceImpl.getVoteStatistics(poll.getId());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_NOT_FOUND, exception.getReason());
    }
}
