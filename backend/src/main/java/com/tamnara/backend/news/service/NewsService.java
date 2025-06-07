package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;

import java.util.List;

public interface NewsService {
    HotissueNewsListResponse getHotissueNewsCardPage();
    MultiCategoryResponse getMultiCategoryPage(Long userId, Integer offset);
    Object getSingleCategoryPage(Long userId, String category, Integer offset);
    NewsListResponse getSearchNewsCardPage(Long userId, List<String> tags, Integer offset);
    NewsDetailDTO getNewsDetail(Long newsId, Long userId);
    NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailDTO update(Long newsId, Long userId, boolean isHotissue);
    void delete(Long newsId, Long userId);

    void createHotissueNews();
}
