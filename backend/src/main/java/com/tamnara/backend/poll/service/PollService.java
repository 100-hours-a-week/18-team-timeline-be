package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.dto.response.PollStatisticsResponse;

public interface PollService {
    Long createPoll(PollCreateRequest request);
    Poll getPollById(Long pollId);
    PollInfoResponse getLatestPublishedPoll(Long userId);
    void schedulePoll(Long pollId);
    PollStatisticsResponse getPollStatistics(Long pollId);

    void updatePollStates();
}
