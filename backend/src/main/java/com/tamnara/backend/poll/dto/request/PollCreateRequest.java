package com.tamnara.backend.poll.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PollCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @Min(1)
    private int minChoices;

    @Min(1)
    private int maxChoices;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @Size(min = 2)
    @Valid
    private List<PollOptionCreateRequest> options;
}
