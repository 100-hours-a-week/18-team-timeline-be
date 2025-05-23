package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.WrappedDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncAIService {
    CompletableFuture<WrappedDTO<StatisticsDTO>> getAIStatistics(String endpoint, List<String> keywords, Integer num);
}
