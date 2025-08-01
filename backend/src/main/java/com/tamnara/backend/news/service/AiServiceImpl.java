package com.tamnara.backend.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.AIException;
import com.tamnara.backend.news.constant.NewsExternalApiEndpoint;
import com.tamnara.backend.news.constant.NewsServiceConstant;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.request.AIHotissueRequest;
import com.tamnara.backend.news.dto.request.AINewsRequest;
import com.tamnara.backend.news.dto.request.AIStatisticsRequest;
import com.tamnara.backend.news.dto.request.AITimelineMergeRequest;
import com.tamnara.backend.news.dto.response.AIHotissueResponse;
import com.tamnara.backend.news.dto.response.AINewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final WebClient aiWebClient;

    @Override
    public WrappedDTO<AINewsResponse> createAINews(List<String> keywords, LocalDate startAt, LocalDate endAt) {
        AINewsRequest aiNewsRequest = new AINewsRequest(
                keywords,
                startAt,
                endAt
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return aiWebClient.post()
                .uri(NewsExternalApiEndpoint.TIMELINE_AI_ENDPOINT)
                .bodyValue(aiNewsRequest)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse
                                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AINewsResponse>>() {})
                                .flatMap(errorBody -> Mono.error(new AIException(clientResponse.statusCode(), errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AINewsResponse>>() {})
                .block();
    }

    @Override
    public List<TimelineCardDTO> mergeTimelineCards(List<TimelineCardDTO> timeline) {
        // 1. 1일카드 -> 1주카드
        timeline = mergeAITimelineCards(timeline, TimelineCardType.DAY, 7);

        // 2. 1주카드 -> 1달카드
        timeline = mergeAITimelineCards(timeline, TimelineCardType.WEEK, 4);

        // 3. 1달카드: 3개월 지남 -> 삭제
        timeline.removeIf(tc -> (TimelineCardType.valueOf(tc.getDuration()) == TimelineCardType.MONTH)
                && (tc.getStartAt().isBefore(LocalDate.now().minusMonths(3))));

        return timeline;
    }

    @Override
    public WrappedDTO<AIHotissueResponse> createAIHotissueKeywords() {
        AIHotissueRequest aiHotissueRequest = new AIHotissueRequest(NewsServiceConstant.HOTISSUE_CREATE_CNT);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return aiWebClient.post()
                .uri(NewsExternalApiEndpoint.HOTISSUE_AI_ENDPOINT)
                .bodyValue(aiHotissueRequest)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse
                                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AIHotissueResponse>>() {})
                                .flatMap(errorBody -> Mono.error(new AIException(clientResponse.statusCode(), errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AIHotissueResponse>>() {})
                .block();
    }

    @Async
    @Override
    public CompletableFuture<WrappedDTO<StatisticsDTO>> getAIStatistics(List<String> keywords) {
        AIStatisticsRequest req = new AIStatisticsRequest(
                keywords,
                NewsServiceConstant.STATISTICS_AI_SEARCH_CNT
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

    private List<TimelineCardDTO> mergeAITimelineCards(List<TimelineCardDTO> timeline, TimelineCardType duration, Integer countNum) {
        timeline.sort(Comparator.comparing(TimelineCardDTO::getStartAt));

        List<TimelineCardDTO> mergedList = new ArrayList<>();
        List<TimelineCardDTO> temp = new ArrayList<>();

        int count = 0;

        for (TimelineCardDTO tc : timeline) {
            if (TimelineCardType.valueOf(tc.getDuration()) != duration) {
                mergedList.add(tc);
                continue;
            }

            temp.add(tc);
            count++;

            if (count == countNum) {
                AITimelineMergeRequest mergeRequest = new AITimelineMergeRequest(temp);

                WrappedDTO<TimelineCardDTO> merged = aiWebClient.post()
                        .uri(NewsExternalApiEndpoint.MERGE_AI_ENDPOINT)
                        .bodyValue(mergeRequest)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                clientResponse -> clientResponse
                                        .bodyToMono(new ParameterizedTypeReference<WrappedDTO<TimelineCardDTO>>() {})
                                        .flatMap(errorBody -> Mono.error(new AIException(clientResponse.statusCode(), errorBody)))
                        )
                        .bodyToMono(new ParameterizedTypeReference<WrappedDTO<TimelineCardDTO>>() {})
                        .block();

                mergedList.add(Objects.requireNonNull(merged).getData());

                temp.clear();
                count = 0;
            }
        }

        mergedList.addAll(temp);
        temp.clear();

        timeline = mergedList;
        timeline.sort(Comparator.comparing(TimelineCardDTO::getStartAt).reversed());

        return timeline;
    }
}
