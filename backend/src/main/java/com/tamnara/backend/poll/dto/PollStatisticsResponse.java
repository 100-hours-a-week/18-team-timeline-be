package com.tamnara.backend.poll.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PollStatisticsResponse {
    private Long pollId;
    private List<OptionResult> results;
    private long totalVotes;
}
