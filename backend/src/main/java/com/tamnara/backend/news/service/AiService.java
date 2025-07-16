package com.tamnara.backend.news.service;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.response.AIHotissueResponse;
import com.tamnara.backend.news.dto.response.AINewsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiService {
    WrappedDTO<AINewsResponse> createAINews(List<String> keywords, LocalDate startAt, LocalDate endAt);
    List<TimelineCardDTO> mergeTimelineCards(List<TimelineCardDTO> timeline);
    WrappedDTO<AIHotissueResponse> createAIHotissueKeywords();
    CompletableFuture<WrappedDTO<StatisticsDTO>> getAIStatistics(List<String> keywords);
}
