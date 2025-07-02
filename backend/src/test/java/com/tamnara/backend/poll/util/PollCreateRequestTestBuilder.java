package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;

import java.util.List;

public class PollCreateRequestTestBuilder {
    public static PollCreateRequest build(
            String title,
            int minChoices,
            int maxChoices,
            List<PollOptionCreateRequest> options
    ) {
        return PollCreateRequest.builder()
                .title(title)
                .minChoices(minChoices)
                .maxChoices(maxChoices)
                .options(options)
                .build();
    }
}
