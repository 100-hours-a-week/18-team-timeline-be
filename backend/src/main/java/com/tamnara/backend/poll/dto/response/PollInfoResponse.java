package com.tamnara.backend.poll.dto.response;

import com.tamnara.backend.poll.dto.PollInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PollInfoResponse {
    private Boolean hasVoted;
    private PollInfoDTO poll;
}
