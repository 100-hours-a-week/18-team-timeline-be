package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;

public interface NewsService {
    HotissueNewsListResponse getHotissueNewsCardPage();
    Object getSingleCategoryPage(Long userId, String category, Integer page, Integer size);
    MultiCategoryResponse getMultiCategoryPage(Long userId, Integer page, Integer size);
    NewsDetailDTO getNewsDetail(Long newsId, Long userId);
    NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailDTO update(Long newsId, Long userId);
    void delete(Long newsId, Long userId);
}
