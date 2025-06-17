package com.tamnara.backend.poll.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.VoteStatistics;
import com.tamnara.backend.poll.util.PollOptionTestBuilder;
import com.tamnara.backend.poll.util.PollTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VoteStatisticsRepositoryTest {

    @Autowired private PollRepository pollRepository;
    @Autowired private PollOptionRepository pollOptionRepository;
    @Autowired private VoteStatisticsRepository voteStatisticsRepository;

    @Test
    @DisplayName("기본 통계 객체 생성과 저장, 조회 성공")
    void saveAndFindByPollId() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        VoteStatistics stat = voteStatisticsRepository.save(VoteStatistics.zero(poll, option));

        // when
        List<VoteStatistics> result = voteStatisticsRepository.findByPollId(poll.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(0L);
        assertThat(result.get(0).getPoll().getId()).isEqualTo(poll.getId());
    }

    @Test
    @DisplayName("PollId + OptionId 조합으로 VoteStatistics 단건 조회 성공")
    void findByPollIdAndOptionId() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        voteStatisticsRepository.save(VoteStatistics.zero(poll, option));

        // when
        Optional<VoteStatistics> result = voteStatisticsRepository.findByPollIdAndOptionId(poll.getId(), option.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getOption().getId()).isEqualTo(option.getId());
    }

    @Test
    @DisplayName("PollId + OptionId 중복 저장 시 예외 발생")
    void duplicatePollAndOptionThrowsException() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        voteStatisticsRepository.save(VoteStatistics.zero(poll, option));

        // when & then
        VoteStatistics duplicate = VoteStatistics.zero(poll, option);
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> voteStatisticsRepository.saveAndFlush(duplicate)
        );
    }
}
