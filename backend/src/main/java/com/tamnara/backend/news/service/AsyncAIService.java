package com.tamnara.backend.news.service;

import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.global.dto.WrappedDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AsyncAIService {
    CompletableFuture<WrappedDTO<StatisticsDTO>> getAIStatisticsDTO(String endpoint, List<String> keywords, Integer num);
}
