package com.tamnara.backend.news.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class AINewsRequest {
    @Size(min = 1, max = 6, message = "키워드는 최소 1개 이상, 최대 6개까지 포함해야 합니다.")
    private List<String> query;
    private LocalDate startAt;
    private LocalDate endAt;
}
