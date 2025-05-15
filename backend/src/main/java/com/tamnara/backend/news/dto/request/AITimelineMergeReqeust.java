package com.tamnara.backend.news.dto.request;

import com.tamnara.backend.news.dto.TimelineCardDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AITimelineMergeReqeust {
    @NotNull(message = "타임라인 카드는 비어 있을 수 없습니다.")
    @Size(min = 1, message = "최소 1개 이상의 카드가 포함되어야 합니다.")
    private List<TimelineCardDTO> timeline;
}
