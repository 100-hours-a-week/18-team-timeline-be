package com.tamnara.backend.poll.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionResult {
    private Long optionId;
    private String title;
    private long count;
}
