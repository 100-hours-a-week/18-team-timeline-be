package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.VoteStatistics;

import java.time.LocalDateTime;

public class VoteStatisticsBuilder {
    public static VoteStatistics buildNew(Poll poll, PollOption option, long count) {
        return VoteStatistics.builder()
                .poll(poll)
                .option(option)
                .count(count)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static VoteStatistics buildUpdated(VoteStatistics original, long newCount) {
        return VoteStatistics.builder()
                .id(original.getId())
                .poll(original.getPoll())
                .option(original.getOption())
                .count(newCount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
