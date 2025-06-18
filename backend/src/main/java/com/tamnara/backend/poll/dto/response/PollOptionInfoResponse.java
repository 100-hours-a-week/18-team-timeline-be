package com.tamnara.backend.poll.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PollOptionInfoResponse {
    private Long id;
    private String title;
    private String imageUrl;
}
