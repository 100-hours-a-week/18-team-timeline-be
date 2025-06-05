package com.tamnara.backend.poll.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.util.PollTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PollRepositoryTest {
    @MockBean
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private PollRepository pollRepository;

    @Test
    @DisplayName("Poll 저장 및 조회에 성공한다")
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
        Poll saved = pollRepository.save(poll);
        Poll found = pollRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getTitle()).isEqualTo("테스트 투표");
        assertThat(found.getState()).isEqualTo(PollState.PUBLISHED);
        assertThat(found.getMinChoices()).isEqualTo(1);
    }

    @Test
    @DisplayName("state==PUBLISHED인 투표 조회에 성공한다")
    void findByState() {
        // given
        Poll openPoll = PollTestBuilder.defaultPoll();
        pollRepository.save(openPoll);

        // when
        List<Poll> result = pollRepository.findByState(PollState.PUBLISHED);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getState()).isEqualTo(PollState.PUBLISHED);
    }

    @Test
    @DisplayName("종료 시간이 현재 이후인 투표 조회에 성공한다")
    void findByEndAtAfter() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Poll poll = PollTestBuilder.defaultPoll();
        pollRepository.save(poll);

        // when
        List<Poll> result = pollRepository.findByEndAtAfter(now);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEndAt()).isAfter(now);
    }
}
