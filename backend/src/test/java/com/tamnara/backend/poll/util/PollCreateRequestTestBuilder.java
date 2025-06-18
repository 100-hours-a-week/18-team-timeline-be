package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;

import java.time.LocalDateTime;
import java.util.List;

public class PollCreateRequestTestBuilder {
    public static PollCreateRequest build(
            String title,
            int minChoices,
            int maxChoices,
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<PollOptionCreateRequest> options
    ) {
        return PollCreateRequest.builder()
                .title(title)
                .minChoices(minChoices)
                .maxChoices(maxChoices)
                .startAt(startAt)
                .endAt(endAt)
                .options(options)
                .build();
    }
}
