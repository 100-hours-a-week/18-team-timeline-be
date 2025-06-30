package com.tamnara.backend.poll.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

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

    @Size(min = 2)
    @Valid
    private List<PollOptionCreateRequest> options;
}
