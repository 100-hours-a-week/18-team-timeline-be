package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;

public interface PollService {
    Long createPoll(PollCreateRequest request);
    Poll getPollById(Long pollId);
    PollInfoResponse getLatestPublishedPoll(Long userId);
    void schedulePoll(Long pollId);

    void updatePollStates();
}
