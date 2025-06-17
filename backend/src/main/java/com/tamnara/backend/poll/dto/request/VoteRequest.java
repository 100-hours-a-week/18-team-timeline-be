package com.tamnara.backend.poll.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {
    @NotEmpty(message = "선택지를 선택해 주세요.")
    private List<Long> optionIds;
}
