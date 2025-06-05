package com.tamnara.backend.poll.util;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;

public class PollOptionTestBuilder {

    public static PollOption build(String title, int sortOrder, String imageUrl, Poll poll) {
        return PollOption.builder()
                .title(title)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .poll(poll)
                .build();
    }

    public static PollOption defaultOption(Poll poll) {
        return build("기본 옵션", 0, "https://example.com/image.png", poll);
    }
}
