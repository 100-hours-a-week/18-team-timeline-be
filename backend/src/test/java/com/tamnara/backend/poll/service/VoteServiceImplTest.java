package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.constant.PollResponseMessage;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceImplTest {

    @Mock private PollRepository pollRepository;
    @Mock private PollOptionRepository pollOptionRepository;
    @Mock private VoteRepository voteRepository;

    @InjectMocks private VoteServiceImpl voteServiceImpl;

    private User user;
    private Poll poll;
    private PollOption option1;
    private PollOption option2;
    private PollOption option3;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        poll = Poll.builder()
                .id(1L)
                .minChoices(1)
                .maxChoices(2)
                .startAt(LocalDateTime.now().minusHours(1))
                .endAt(LocalDateTime.now().plusHours(1))
                .state(PollState.PUBLISHED)
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
    }

    @Test
    @DisplayName("투표 기록 저장에 성공")
    void vote_success() {
        // given
        VoteRequest voteRequest = new VoteRequest(List.of(option1.getId(), option2.getId()));

        when(pollRepository.findLatestPollByPublishedPoll()).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(any())).thenReturn(List.of(option1, option2));
        when(voteRepository.hasVotedLatestPublishedPoll(user.getId())).thenReturn(true);

        // when
        voteServiceImpl.vote(user, voteRequest);

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
            voteServiceImpl.vote(user, new VoteRequest());
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
            voteServiceImpl.vote(user, new VoteRequest());
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
            voteServiceImpl.vote(user, new VoteRequest());
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
        when(voteRepository.hasVotedLatestPublishedPoll(user.getId())).thenReturn(false);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            voteServiceImpl.vote(user, new VoteRequest(List.of(101L)));
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
            voteServiceImpl.vote(user, new VoteRequest(List.of(101L, 102L, 103L)));
        });

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(PollResponseMessage.POLL_INVALID_SELECTION_COUNT, exception.getReason());
    }
}
