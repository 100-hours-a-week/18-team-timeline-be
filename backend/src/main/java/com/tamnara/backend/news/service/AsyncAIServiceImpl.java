package com.tamnara.backend.news.service;

import com.tamnara.backend.global.exception.AIException;
import com.tamnara.backend.news.constant.NewsExternalApiEndpoint;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.news.dto.request.AIStatisticsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AsyncAIServiceImpl implements AsyncAIService {

    private final WebClient aiWebClient;

    @Async
    public CompletableFuture<WrappedDTO<StatisticsDTO>> getAIStatistics(List<String> keywords, Integer num) {
        AIStatisticsRequest req = new AIStatisticsRequest(
                keywords,
                num
        );

        return aiWebClient.post()
                .uri(NewsExternalApiEndpoint.STATISTIC_AI_ENDPOINT)
                .bodyValue(req)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse
                                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<StatisticsDTO>>() {})
                                .flatMap(errorBody -> Mono.error(new AIException(clientResponse.statusCode(), errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<StatisticsDTO>>() {})
                .toFuture();
    }
}
