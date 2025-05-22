package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.*;
import com.tamnara.backend.news.dto.request.*;

import java.util.List;
import java.util.Map;

public interface NewsService {
    List<NewsCardDTO> getHotissueNewsCardPage(Long userId);
    List<NewsCardDTO> getNormalNewsCardPage(Long userId, Integer page, Integer size);
    List<NewsCardDTO> getNormalNewsCardPageByCategory(Long userId, String category, Integer page, Integer size);
    Map<String, List<NewsCardDTO>> getNormalNewsCardPages(Long userId, Integer page, Integer size);
    NewsDetailDTO getNewsDetail(Long newsId, Long userId);
    NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailDTO update(Long newsId, Long userId);
    void delete(Long newsId, Long userId);
}
