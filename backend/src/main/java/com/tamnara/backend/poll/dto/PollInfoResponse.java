package com.tamnara.backend.poll.dto;

import com.tamnara.backend.poll.domain.Poll;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class PollInfoResponse {

    private String title;
    private int minChoices;
    private int maxChoices;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private List<PollOptionInfoResponse> options;

    public PollInfoResponse(Poll poll) {
        this.title = poll.getTitle();
        this.minChoices = poll.getMinChoices();
        this.maxChoices = poll.getMaxChoices();
        this.startAt = poll.getStartAt();
        this.endAt = poll.getEndAt();
        this.options = poll.getOptions().stream()
                .map(option -> new PollOptionInfoResponse(option.getTitle(), option.getImageUrl()))
                .collect(Collectors.toList());
    }
}
