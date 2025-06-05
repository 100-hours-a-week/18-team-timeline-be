package com.tamnara.backend.poll.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tamnara.backend.poll.domain.*;
import com.tamnara.backend.poll.util.PollOptionTestBuilder;
import com.tamnara.backend.poll.util.VoteTestBuilder;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import com.tamnara.backend.poll.util.PollTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static com.tamnara.backend.utils.CreateUserUtils.createActiveUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VoteRepositoryTest {
    @MockBean
    private JPAQueryFactory jpaQueryFactory; // NewsSearchRepositoryImpl 로딩 시 필요

    @Autowired private PollRepository pollRepository;
    @Autowired private PollOptionRepository pollOptionRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setup() {
        this.savedUser = userRepository.save(createActiveUser(
                "test@user.com", "tester", "KAKAO", "123"));
    }

    @Test
    @DisplayName("UserId와 PollId 조합으로 Vote 저장 및 조회에 성공한다")
    void saveVoteAndFindByUserAndPoll() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        Vote vote = voteRepository.save(VoteTestBuilder.build(savedUser, poll, option));

        // when
        List<Vote> result = voteRepository.findByUserIdAndPollId(savedUser.getId(), poll.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Poll ID로 전체 투표 기록 조회에 성공한다")
    void findByPollId() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        Vote vote = voteRepository.save(VoteTestBuilder.build(savedUser, poll, option));

        // when
        List<Vote> votes = voteRepository.findByPollId(poll.getId());

        // then
        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getPoll().getId()).isEqualTo(poll.getId());
    }

    @Test
    @DisplayName("PollID와 OptionID 조합으로 투표 수 조회에 성공한다")
    void countVotesByPollAndOption() {
        // given
        Poll poll = pollRepository.save(PollTestBuilder.defaultPoll());
        PollOption option = pollOptionRepository.save(PollOptionTestBuilder.defaultOption(poll));
        Vote vote = voteRepository.save(VoteTestBuilder.build(savedUser, poll, option));

        // when
        long count = voteRepository.countByPollIdAndOptionId(poll.getId(), option.getId());

        // then
        assertThat(count).isEqualTo(1);
    }
}
