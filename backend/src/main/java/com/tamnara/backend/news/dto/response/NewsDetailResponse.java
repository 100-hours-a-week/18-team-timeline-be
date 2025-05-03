package com.tamnara.backend.news.dto.response;

import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class NewsDetailResponse {
    @Length(max = 18, message = "타임라인 카드의 제목은 24자까지만 가능합니다.")
    private String title;
    @PastOrPresent(message = "날짜는 과거 또는 현재만 가능합니다.")
    private LocalDateTime updatedAt;
    private boolean bookmarked;
    @NotNull(message = "타임라인 카드는 비어 있을 수 없습니다.")
    @Size(min = 1, message = "최소 1개 이상의 카드가 포함되어야 합니다.")
    private List<TimelineCardDTO> timeline;
    private StatisticsDTO statistics;
}
