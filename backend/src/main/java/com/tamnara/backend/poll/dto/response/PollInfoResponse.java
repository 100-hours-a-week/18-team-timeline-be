package com.tamnara.backend.poll.dto.response;

import com.tamnara.backend.poll.dto.PollInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PollInfoResponse {
    private Boolean hasVoted;
    private List<Long> votedOptions;
    private PollInfoDTO poll;
}
