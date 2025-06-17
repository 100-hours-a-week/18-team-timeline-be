package com.tamnara.backend.poll.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.Vote;
import com.tamnara.backend.poll.util.PollOptionTestBuilder;
import com.tamnara.backend.poll.util.PollTestBuilder;
import com.tamnara.backend.poll.util.VoteTestBuilder;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static com.tamnara.backend.global.util.CreateUserUtil.createActiveUser;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VoteRepositoryTest {

    @PersistenceContext
    private EntityManager em;
    
    @Autowired private PollRepository pollRepository;
    @Autowired private PollOptionRepository pollOptionRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setup() {
        this.savedUser = userRepository.saveAndFlush(createActiveUser(
                "test@user.com", "tester", "KAKAO", "123"));
        em.clear();
    }

    @Test
    @DisplayName("UserId와 PollId 조합으로 Vote 저장 및 조회 성공")
    void saveVoteAndFindByUserAndPoll() {
        // given
        Poll poll = pollRepository.saveAndFlush(PollTestBuilder.defaultPoll());
        em.clear();
        PollOption option = pollOptionRepository.saveAndFlush(PollOptionTestBuilder.defaultOption(poll));
        em.clear();
        voteRepository.saveAndFlush(VoteTestBuilder.build(savedUser, poll, option));
        em.clear();

        // when
        List<Vote> result = voteRepository.findByUserIdAndPollId(savedUser.getId(), poll.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Poll ID로 전체 투표 기록 조회 성공")
    void findByPollId() {
        // given
        Poll poll = pollRepository.saveAndFlush(PollTestBuilder.defaultPoll());
        em.clear();
        PollOption option = pollOptionRepository.saveAndFlush(PollOptionTestBuilder.defaultOption(poll));
        em.clear();
        voteRepository.saveAndFlush(VoteTestBuilder.build(savedUser, poll, option));
        em.clear();

        // when
        List<Vote> votes = voteRepository.findByPollId(poll.getId());

        // then
        assertThat(votes).hasSize(1);
        assertThat(votes.get(0).getPoll().getId()).isEqualTo(poll.getId());
    }

    @Test
    @DisplayName("PollID와 OptionID 조합으로 투표 수 조회 성공")
    void countVotesByPollAndOption() {
        // given
        Poll poll = pollRepository.saveAndFlush(PollTestBuilder.defaultPoll());
        em.clear();
        PollOption option = pollOptionRepository.saveAndFlush(PollOptionTestBuilder.defaultOption(poll));
        em.clear();
        voteRepository.saveAndFlush(VoteTestBuilder.build(savedUser, poll, option));
        em.clear();

        // when
        long count = voteRepository.countByPollIdAndOptionId(poll.getId(), option.getId());

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("UserId로 최신 공개 투표에 대한 투표 여부 조회 성공")
    void hasVotedLatestPublishedPoll() {
        // given
        Poll poll = pollRepository.saveAndFlush(PollTestBuilder.defaultPoll());
        em.clear();
        PollOption option = pollOptionRepository.saveAndFlush(PollOptionTestBuilder.defaultOption(poll));
        em.clear();
        voteRepository.saveAndFlush(VoteTestBuilder.build(savedUser, poll, option));
        em.clear();
        
        // when
        Boolean hasVoted = voteRepository.hasVotedLatestPublishedPoll(savedUser.getId());

        // then
        assertThat(hasVoted).isTrue();
    }
}
