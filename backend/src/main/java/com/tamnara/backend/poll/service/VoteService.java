package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollIdResponse;
import com.tamnara.backend.user.domain.User;

public interface VoteService {
    PollIdResponse vote(User user, VoteRequest voteRequest);
}
