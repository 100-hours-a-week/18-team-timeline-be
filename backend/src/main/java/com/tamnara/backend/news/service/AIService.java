package com.tamnara.backend.news.service;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.response.AINewsResponse;

import java.time.LocalDate;
import java.util.List;

public interface AIService {
    WrappedDTO<AINewsResponse> createAINews(List<String> keywords, LocalDate startAt, LocalDate endAt);
    List<TimelineCardDTO> mergeTimelineCards(List<TimelineCardDTO> timeline);
}
