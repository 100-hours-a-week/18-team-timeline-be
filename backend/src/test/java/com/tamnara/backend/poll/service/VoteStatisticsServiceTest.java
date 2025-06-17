package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.dto.PollStatisticsResponse;
import com.tamnara.backend.poll.exception.PollNotFoundException;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.repository.VoteStatisticsRepository;
import com.tamnara.backend.poll.util.PollOptionTestBuilder;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.poll.util.VoteStatisticsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoteStatisticsServiceTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private VoteStatisticsRepository voteStatisticsRepository;

    @InjectMocks
    private VoteStatisticsService voteStatisticsService;

    private Poll poll;
    private PollOption option;
    private VoteStatistics statistics;

    @BeforeEach
    void setUp() {
        poll = PollTestBuilder.defaultPoll();
        option = PollOptionTestBuilder.defaultOption(poll);
        poll.getOptions().add(option);
        statistics = VoteStatisticsBuilder.buildNew(poll, option, 5L);
    }

    @Test
    @DisplayName("generateAllStatistics 실행 시 통계가 업데이트 된다")
    void generateAllStatistics_updatesStatisticsCorrectly() {
        // given
        when(pollRepository.findByState(PollState.PUBLISHED)).thenReturn(List.of(poll));
        when(voteRepository.countByPollIdAndOptionId(poll.getId(), option.getId())).thenReturn(5L);
        when(voteStatisticsRepository.findByPollIdAndOptionId(poll.getId(), option.getId())).thenReturn(Optional.empty());

        // when
        voteStatisticsService.generateAllStatistics();

        // then
        verify(voteStatisticsRepository).save(any(VoteStatistics.class));
    }

    @Test
    @DisplayName("getPollStatistics는 올바른 통계 정보를 반환한다")
    void getPollStatistics_returnsCorrectResponse() {
        // given
        when(pollRepository.findById(poll.getId())).thenReturn(Optional.of(poll));
        when(voteStatisticsRepository.findByPollIdAndOptionId(poll.getId(), option.getId())).thenReturn(Optional.of(statistics));

        // when
        PollStatisticsResponse response = voteStatisticsService.getPollStatistics(poll.getId());

        // then
        assertThat(response.getPollId()).isEqualTo(poll.getId());
        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getCount()).isEqualTo(5);
        assertThat(response.getTotalVotes()).isEqualTo(5);
    }

    @Test
    @DisplayName("getPollStatistics는 Poll이 존재하지 않을 경우 예외를 발생시킨다")
    void getPollStatistics_throwsException_whenPollNotFound() {
        // given
        when(pollRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> voteStatisticsService.getPollStatistics(1L))
                .isInstanceOf(PollNotFoundException.class)
                .hasMessage(POLL_NOT_FOUND);
    }
}
