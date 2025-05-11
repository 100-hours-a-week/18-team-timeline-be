package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.*;
import com.tamnara.backend.news.dto.request.*;
import com.tamnara.backend.news.dto.response.*;

import java.util.List;
import java.util.Map;

public interface NewsService {
    List<NewsCardDTO> getHotissueNewsCardPage(Long userId);
    List<NewsCardDTO> getNormalNewsCardPage(Long userId, Integer page, Integer size);
    List<NewsCardDTO> getNormalNewsCardPage(Long userId, String category, Integer page, Integer size);
    Map<String, List<NewsCardDTO>> getNormalNewsCardPages(Long userId, Integer page, Integer size);
    NewsDetailResponse getNewsDetail(Long newsId, Long userId);
    NewsDetailResponse save(Long userId, boolean isHotissue, NewsCreateRequest req);
    NewsDetailResponse update(Long newsId, Long userId);
    Long delete(Long newsId, Long userId);
}
