package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.user.domain.User;

public interface VoteService {
    void vote(User user, VoteRequest voteRequest);
}
