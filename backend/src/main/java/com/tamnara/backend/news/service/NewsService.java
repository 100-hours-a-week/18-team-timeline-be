package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.*;
import com.tamnara.backend.news.dto.request.*;
import com.tamnara.backend.news.dto.response.*;

import java.util.List;

public interface NewsService {
    List<NewsCardDTO> getNewsPage(Long userId, boolean isHotissue, Integer page, Integer size);
    List<NewsCardDTO> getNewsPage(Long userId, boolean isHotissue, String category, Integer page, Integer size);
    NewsDetailResponse getNewsDetail(Long newsId, Long userId);
    NewsDetailResponse save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailResponse update(Long newsId, Long userId);
    Long delete(Long newsId, Long userId);
}
