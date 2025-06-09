package com.tamnara.backend.poll.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PollOptionCreateRequest {

    @NotBlank(message = "옵션 제목은 필수입니다.")
    private String title;

    private String imageUrl;  // nullable
}
