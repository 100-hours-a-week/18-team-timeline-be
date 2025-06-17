package com.tamnara.backend.poll.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.PollCreateRequest;
import com.tamnara.backend.poll.dto.PollOptionCreateRequest;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static com.tamnara.backend.poll.constant.PollResponseMessage.MIN_CHOICES_EXCEED_MAX;
import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_FOUND;
import static com.tamnara.backend.poll.constant.PollResponseMessage.PUBLISHED_POLL_ALREADY_EXISTS;
import static com.tamnara.backend.poll.constant.PollResponseMessage.START_DATE_LATER_THAN_END_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PollRepository pollRepository;

    @Mock
    private PollOptionRepository pollOptionRepository;

    @InjectMocks
    private PollService pollService;

    private Poll poll;
    private PollOption option;

    @BeforeEach
    void setUp() {
        poll = PollTestBuilder.defaultPoll();
        option = PollOptionTestBuilder.defaultOption(poll);
    }

    @Test
    @DisplayName("createPoll 실행에 성공한다")
    void createPoll_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        PollCreateRequest request = PollCreateRequestTestBuilder.build(
                "Test Poll", 1, 2, now.minusDays(1), now.plusDays(1),
                Arrays.asList(
                        new PollOptionCreateRequest("Option 1", "url1"),
                        new PollOptionCreateRequest("Option 2", "url2")
                ));

        when(pollRepository.save(Mockito.any(Poll.class))).thenReturn(poll);
        when(pollOptionRepository.saveAll(Mockito.anyList())).thenReturn(Arrays.asList(option));

        // when
        Long pollId = pollService.createPoll(request);

        // then
        assertThat(pollId).isEqualTo(poll.getId());
        Mockito.verify(pollRepository, Mockito.times(1)).save(Mockito.any(Poll.class));
        Mockito.verify(pollOptionRepository, Mockito.times(1)).saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("createPoll 실행에서 minChoices가 maxChoices보다 큰 경우 400 에러가 발생한다")
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
        assertThatThrownBy(() -> pollService.createPoll(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MIN_CHOICES_EXCEED_MAX);
    }

    @Test
    @DisplayName("createPoll 실행에서 start_at이 end_at보다 나중인 경우 400 에러가 발생한다")
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
        assertThatThrownBy(() -> pollService.createPoll(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(START_DATE_LATER_THAN_END_DATE);
    }

    @Test
    @DisplayName("getPollById 실행에서 정상적으로 Poll이 반환된다")
    void getPollById_returnsPoll_whenPollExists() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));

        // when
        Poll result = pollService.getPollById(1L);

        // then
        assertThat(result).isEqualTo(poll);
    }

    @Test
    @DisplayName("getPollById 실행에서 PollId가 존재하지 않는 경우 예외가 발생한다")
    void getPollById_returnsNull_whenPollDoesNotExist() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> pollService.getPollById(1L)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo(POLL_NOT_FOUND);
    }

    @Test
    @DisplayName("schedulePoll을 통해 state 변경에 성공한다")
    void schedulePoll_changesStateToScheduled() {
        // given
        when(pollRepository.save(Mockito.any(Poll.class))).thenReturn(poll);

        // when
        pollService.schedulePoll(poll);

        // then
        assertThat(poll.getState()).isEqualTo(PollState.SCHEDULED);
        Mockito.verify(pollRepository, Mockito.times(1)).save(poll);
    }

    @Test
    @DisplayName("publishPoll을 통해 state 변경에 성공한다")
    void publishPoll_changesStateToPublished() {
        // given
        when(pollRepository.existsByState(PollState.PUBLISHED)).thenReturn(false);
        when(pollRepository.save(Mockito.any(Poll.class))).thenReturn(poll);

        // when
        pollService.publishPoll(poll);

        // then
        assertThat(poll.getState()).isEqualTo(PollState.PUBLISHED);
        Mockito.verify(pollRepository, Mockito.times(1)).save(poll);
    }

    @Test
    @DisplayName("publishPoll에서 이미 PUBLISHED 상태인 투표가 있을 경우 예외를 반환한다")
    void publishPoll_throwsException_whenPollAlreadyPublished() {
        // given
        when(pollRepository.existsByState(PollState.PUBLISHED)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> pollService.publishPoll(poll))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(PUBLISHED_POLL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("deletePoll을 통해 state 변경에 성공한다")
    void deletePoll_changesStateToDeleted() {
        // given
        when(pollRepository.save(Mockito.any(Poll.class))).thenReturn(poll);

        // when
        pollService.deletePoll(poll);

        // then
        assertThat(poll.getState()).isEqualTo(PollState.DELETED);
        Mockito.verify(pollRepository, Mockito.times(1)).save(poll);
    }

    @Test
    @DisplayName("투표 생성 시 전체 알림 발행을 검증한다.")
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

        when(pollRepository.save(Mockito.any(Poll.class))).thenReturn(poll);
        when(pollOptionRepository.saveAll(Mockito.anyList())).thenReturn(Arrays.asList(option));

        // when
        pollService.createPoll(request);

        // then
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Mockito.verify(pollOptionRepository, Mockito.times(1)).saveAll(Mockito.anyList());

        ArgumentCaptor<AlarmEvent> captor = ArgumentCaptor.forClass(AlarmEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        AlarmEvent event = captor.getValue();
        assertEquals(AlarmMessage.POLL_START_TITLE, event.getTitle());
        assertEquals(String.format(AlarmMessage.POLL_START_CONTENT, pollTitle), event.getContent());
        assertEquals(AlarmType.POLLS, event.getTargetType());
        assertNull(event.getTargetId());
    }
}
