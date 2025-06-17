package com.tamnara.backend.news.dto.request;

import com.tamnara.backend.news.dto.TimelineCardDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Getter
@AllArgsConstructor
public class KtbNewsCreateRequest {
    @Length(max = 225, message = "뉴스의 제목은 255자까지만 가능합니다.")
    private String title;
    @Length(max = 225, message = "뉴스의 미리보기 내용은 255자까지만 가능합니다.")
    private String summary;
    @Length(max = 225, message = "뉴스의 이미지 URL은 255자까지만 가능합니다.")
    private String image;
    @NotNull(message = "타임라인 카드는 비어 있을 수 없습니다.")
    @Size(min = 1, message = "최소 1개 이상의 카드가 포함되어야 합니다.")
    private List<TimelineCardDTO> timeline;
}
