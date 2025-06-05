package com.tamnara.backend.poll.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
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
class PollOptionRepositoryTest {
    @MockBean
    private JPAQueryFactory jpaQueryFactory; // NewsSearchRepositoryImpl 로딩 시 필요

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Test
    @DisplayName("PollOption을 저장하고 PollId로 조회한다")
    void saveAndFindByPollId() {
        // given
        Poll poll = PollTestBuilder.build(
                "투표 제목",
                1,
                2,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                PollState.PUBLISHED
        );
        Poll savedPoll = pollRepository.save(poll);

        PollOption option1 = PollOption.builder()
                .title("옵션 1")
                .imageUrl("https://example.com/img1.png")
                .sortOrder(0)
                .poll(savedPoll)
                .build();

        PollOption option2 = PollOption.builder()
                .title("옵션 2")
                .imageUrl("https://example.com/img2.png")
                .sortOrder(1)
                .poll(savedPoll)
                .build();

        pollOptionRepository.saveAll(List.of(option1, option2));

        // when
        List<PollOption> result = pollOptionRepository.findByPollId(savedPoll.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("옵션 1");
        assertThat(result.get(1).getPoll().getId()).isEqualTo(savedPoll.getId());
    }
}
