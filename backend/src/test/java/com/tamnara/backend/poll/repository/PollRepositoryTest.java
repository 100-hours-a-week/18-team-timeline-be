package com.tamnara.backend.poll.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.util.PollTestBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PollRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private PollRepository pollRepository;

    @Test
    @DisplayName("Poll 저장 및 조회 성공")
    void saveAndFindPoll() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Poll poll = PollTestBuilder.build(
                "테스트 투표",
                1,
                2,
                now.minusDays(1),
                now.plusDays(3),
                PollState.PUBLISHED
        );

        // when
        Poll saved = pollRepository.saveAndFlush(poll);
        em.clear();
        Poll found = pollRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("테스트 투표");
        assertThat(found.getState()).isEqualTo(PollState.PUBLISHED);
        assertThat(found.getMinChoices()).isEqualTo(1);
    }

    @Test
    @DisplayName("state==PUBLISHED인 투표 조회 성공")
    void findByState() {
        // given
        Poll openPoll = PollTestBuilder.defaultPoll();
        pollRepository.saveAndFlush(openPoll);
        em.clear();

        // when
        List<Poll> result = pollRepository.findByState(PollState.PUBLISHED);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getState()).isEqualTo(PollState.PUBLISHED);
    }

    @Test
    @DisplayName("종료 시간이 현재 이후인 투표 조회 성공")
    void findByEndAtAfter() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Poll poll = PollTestBuilder.defaultPoll();
        pollRepository.saveAndFlush(poll);
        em.clear();

        // when
        List<Poll> result = pollRepository.findByEndAtAfter(now);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEndAt()).isAfter(now);
    }

    @Test
    @DisplayName("최신 공개 투표 조회")
    void findLatestPollByPublishedPoll() {
        // given
        Poll poll = PollTestBuilder.defaultPoll();
        poll.changeState(PollState.PUBLISHED);
        pollRepository.saveAndFlush(poll);
        em.clear();
        System.out.println(("poll.state:" + poll.getState()));

        // when
        Poll foundPoll = pollRepository.findLatestPollByPublishedPoll().get();

        // then
        assertEquals(poll.getId(), foundPoll.getId());
    }
}
