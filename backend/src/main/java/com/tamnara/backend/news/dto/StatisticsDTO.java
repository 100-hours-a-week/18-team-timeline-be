package com.tamnara.backend.news.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatisticsDTO {
    @Min(value = 0, message = "통계의 값은 음수가 될 수 없습니다.")
    private int positive;
    @Min(value = 0, message = "통계의 값은 음수가 될 수 없습니다.")
    private int neutral;
    @Min(value = 0, message = "통계의 값은 음수가 될 수 없습니다.")
    private int negative;
}
