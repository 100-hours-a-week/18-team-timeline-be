package com.tamnara.backend.poll.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotEmpty(message = "선택한 옵션이 없어야 합니다.")
    private List<Long> optionIds;
}
