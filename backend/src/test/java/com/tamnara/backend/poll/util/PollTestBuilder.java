package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;

import java.time.LocalDateTime;

public class PollTestBuilder {

    public static Poll build(
            String title,
            int minChoices,
            int maxChoices,
            LocalDateTime startAt,
            LocalDateTime endAt,
            PollState state
    ) {
        return Poll.builder()
                .title(title)
                .minChoices(minChoices)
                .maxChoices(maxChoices)
                .startAt(startAt)
                .endAt(endAt)
                .state(state)
                .build();
    }

    public static Poll defaultPoll() {
        LocalDateTime now = LocalDateTime.now();
        return build(
                "테스트용 기본 투표",
                1,
                2,
                now.minusDays(1),
                now.plusDays(3),
                PollState.PUBLISHED
        );
    }
}
