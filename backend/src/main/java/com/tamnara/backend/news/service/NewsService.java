package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;

import java.util.List;

public interface NewsService {
    List<NewsCardDTO> getHotissueNewsCardPage(Long userId);
    List<NewsCardDTO> getNormalNewsCardPage(Long userId, Integer page, Integer size);
    List<NewsCardDTO> getNormalNewsCardPageByCategory(Long userId, String category, Integer page, Integer size);
    NewsDetailDTO getNewsDetail(Long newsId, Long userId);
    NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailDTO update(Long newsId, Long userId);
    void delete(Long newsId, Long userId);
}
