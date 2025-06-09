package com.tamnara.backend.poll.service;

import com.tamnara.backend.global.util.CreateUserUtil;
import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.exception.*;
import com.tamnara.backend.poll.repository.PollOptionRepository;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock private PollRepository pollRepository;
    @Mock private PollOptionRepository pollOptionRepository;
    @Mock private VoteRepository voteRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private VoteService voteService;

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
    @DisplayName("투표 기록 저장에 성공한다")
    void vote_success() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L, 102L))).thenReturn(List.of(option1, option2));
        when(voteRepository.findByUserIdAndPollId(user.getId(), poll.getId())).thenReturn(Collections.emptyList());

        // when
        voteService.vote(poll.getId(), user, List.of(101L, 102L));

        // then
        verify(voteRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("투표 대상 투표가 존재하지 않는 경우 404 예외를 반환한다")
    void vote_pollNotFound() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L)))
                .isInstanceOf(PollNotFoundException.class);
    }

    @Test
    @DisplayName("투표가 PUBLISHED 상태가 아닌 경우 403 예외를 반환한다")
    void vote_pollNotPublished() {
        // given
        poll.changeState(PollState.SCHEDULED);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));

        // when & then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L)))
                .isInstanceOf(PollForbiddenException.class);
    }

    @Test
    @DisplayName("투표 기간이 아닌 경우 403 예외를 반환한다")
    void vote_outOfPeriod() {
        // given
        LocalDateTime now = LocalDateTime.now();
        poll = PollTestBuilder.build("test", 1, 2, now.plusDays(1), now.plusDays(3), PollState.PUBLISHED);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));

        // then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L)))
                .isInstanceOf(PollForbiddenException.class);
    }

    @Test
    @DisplayName("옵션 ID가 잘못된 경우 404 예외를 반환한다")
    void vote_optionNotFound() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L))).thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L)))
                .isInstanceOf(PollNotFoundException.class);
    }

    @Test
    @DisplayName("이미 투표한 경우 409 예외를 반환한다")
    void vote_alreadyVoted() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L))).thenReturn(List.of(option1));
        when(voteRepository.findByUserIdAndPollId(user.getId(), poll.getId())).thenReturn(List.of(mock(Vote.class)));

        // when & then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L)))
                .isInstanceOf(PollConflictException.class);
    }

    @Test
    @DisplayName("선택된 옵션 수가 범위를 벗어나는 경우 400 예외를 반환한다")
    void vote_optionCountInvalid() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(pollOptionRepository.findAllById(List.of(101L, 102L, 103L)))
                .thenReturn(List.of(option1, option2, option3));

        // when & then
        assertThatThrownBy(() -> voteService.vote(1L, user, List.of(101L, 102L, 103L)))
                .isInstanceOf(PollBadRequestException.class);
    }
}
