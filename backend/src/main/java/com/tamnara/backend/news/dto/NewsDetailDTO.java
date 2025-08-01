package com.tamnara.backend.news.dto;

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
public class NewsDetailDTO {
    private Long id;
//    @Length(max = 18, message = "타임라인 카드의 제목은 18자까지만 가능합니다.")
    private String title;
    @Length(max = 255, message = "이미지 링크의 길이가 너무 깁니다.")
    private String image;
    private String category;
    @PastOrPresent(message = "날짜는 과거 또는 현재만 가능합니다.")
    private LocalDateTime updatedAt;
    private boolean bookmarked;
    @NotNull(message = "타임라인 카드는 비어 있을 수 없습니다.")
    @Size(min = 1, message = "최소 1개 이상의 카드가 포함되어야 합니다.")
    private List<TimelineCardDTO> timeline;
    private StatisticsDTO statistics;
}
