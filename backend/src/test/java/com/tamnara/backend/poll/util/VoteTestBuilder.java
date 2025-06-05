package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.Vote;
import com.tamnara.backend.user.domain.User;

import java.time.LocalDateTime;

public class VoteTestBuilder {

    public static Vote build(User user, Poll poll, PollOption option) {
        return Vote.builder()
                .user(user)
                .poll(poll)
                .option(option)
                .createdAt(LocalDateTime.now())
                .votedAt(LocalDateTime.now())
                .build();
    }
}
