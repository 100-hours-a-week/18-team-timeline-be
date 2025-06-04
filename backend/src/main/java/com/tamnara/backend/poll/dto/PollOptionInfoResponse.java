package com.tamnara.backend.poll.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PollOptionInfoResponse {
    private String title;
    private String imageUrl;
}

