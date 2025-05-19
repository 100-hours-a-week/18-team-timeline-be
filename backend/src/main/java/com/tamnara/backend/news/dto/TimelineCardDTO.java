package com.tamnara.backend.news.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.util.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TimelineCardDTO {
//    @Length(max = 18, message = "타임라인 카드의 제목은 18자까지만 가능합니다.")
    private String title;
    private String content;
    private List<String> source;
    @ValueOfEnum(enumClass = TimelineCardType.class, message = "타임라인 카드 종류가 올바르지 않습니다.")
    private String duration;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endAt;
}
