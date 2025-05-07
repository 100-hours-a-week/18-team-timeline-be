package com.tamnara.backend.news.dto;

import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.util.ValueOfEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NewsCardDTO {
    private Long id;
    @Length(max = 24, message = "뉴스의 제목은 24자까지만 가능합니다.")
    private String title;
    @Length(max = 36, message = "뉴스의 미리보기 내용은 36자까지만 가능합니다.")
    private String summary;
    @Length(max = 255, message = "이미지 링크의 길이가 너무 깁니다.")
    private String imageUrl;
    @ValueOfEnum(enumClass = CategoryType.class, message = "카테고리 값이 올바르지 않습니다.")
    private String category;
    private LocalDateTime updatedAt;
    private boolean bookmarked;
    private LocalDateTime bookmarkedAt;
}
