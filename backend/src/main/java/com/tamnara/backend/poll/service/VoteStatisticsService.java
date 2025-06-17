package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.dto.response.PollStatisticsResponse;

public interface VoteStatisticsService {
    void generateAllStatistics();
    PollStatisticsResponse getPollStatistics(Long pollId);

}
