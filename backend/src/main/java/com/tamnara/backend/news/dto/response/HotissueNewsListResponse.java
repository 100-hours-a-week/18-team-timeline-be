package com.tamnara.backend.news.dto.response;

import com.tamnara.backend.news.dto.NewsCardDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HotissueNewsListResponse {
    private List<NewsCardDTO> newsList;
}
