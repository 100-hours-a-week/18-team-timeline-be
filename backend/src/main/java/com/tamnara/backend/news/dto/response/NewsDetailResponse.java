package com.tamnara.backend.news.dto.response;

import com.tamnara.backend.news.dto.NewsDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsDetailResponse {
    private NewsDetailDTO news;
}
