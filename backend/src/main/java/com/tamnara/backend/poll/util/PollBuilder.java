package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PollBuilder {

    public static Poll buildPollFromRequest(PollCreateRequest request) {
        return Poll.builder()
                .title(request.getTitle())
                .minChoices(request.getMinChoices())
                .maxChoices(request.getMaxChoices())
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now())
                .state(PollState.DRAFT)
                .build();
    }

    public static List<PollOption> buildPollOptionsFromRequest(List<PollOptionCreateRequest> optionRequests, Poll poll) {
        List<PollOption> options = new ArrayList<>();
        int sortOrder = 0;

        for (PollOptionCreateRequest optionReq : optionRequests) {
            PollOption option = PollOption.builder()
                    .title(optionReq.getTitle())
                    .imageUrl(optionReq.getImageUrl())
                    .sortOrder(sortOrder++)
                    .poll(poll)
                    .build();
            options.add(option);
        }

        return options;
    }
}
