package com.tamnara.backend.news.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.global.exception.AIException;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.domain.NewsTag;
import com.tamnara.backend.news.domain.Tag;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.WrappedDTO;
import com.tamnara.backend.news.dto.request.AINewsRequest;
import com.tamnara.backend.news.dto.request.AITimelineMergeRequest;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.AINewsResponse;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.repository.CategoryRepository;
import com.tamnara.backend.news.repository.NewsImageRepository;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.news.repository.NewsTagRepository;
import com.tamnara.backend.news.repository.TagRepository;
import com.tamnara.backend.news.repository.TimelineCardRepository;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final WebClient aiWebClient;
    private final AsyncAIService asyncAiService;

    private final NewsRepository newsRepository;
    private final TimelineCardRepository timelineCardRepository;
    private final NewsImageRepository newsImageRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NewsTagRepository newsTagRepository;

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    private final String TIMELINE_AI_ENDPOINT = "/timeline";
    private final String MERGE_AI_ENDPOINT = "/merge";
    private final String STATISTIC_AI_ENDPOINT = "/comment";
    private final String HOTISSUE_AI_ENDPOINT = "/hot";

    private final Integer STATISTICS_AI_SEARCH_CNT = 10;
    private final Integer NEWS_CREATE_DAYS = 30;
    private final Integer NEWS_UPDATE_HOURS = 24;
    private final Integer NEWS_DELETE_DAYS = 90;

    @Override
    public List<NewsCardDTO> getHotissueNewsCardPage(Long userId) {
        Page<News> newsPage = newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged());
        return getNewsCardDTOList(userId, newsPage);
    }

    @Override
    public List<NewsCardDTO> getNormalNewsCardPage(Long userId, Integer page, Integer size) {
        Page<News> newsPage = newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page, size));
        return getNewsCardDTOList(userId, newsPage);
    }

    @Override
    public List<NewsCardDTO> getNormalNewsCardPageByCategory(Long userId, String category, Integer page, Integer size) {
        Page<News> newsPage;
        if (category != null) {
            Category c = categoryRepository.findByName(CategoryType.valueOf(category.toUpperCase()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."));
            newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(c.getId(), PageRequest.of(page, size));
        } else {
            newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(null, PageRequest.of(page, size));
        }
        return getNewsCardDTOList(userId, newsPage);
    }

    @Override
    public Map<String, List<NewsCardDTO>> getNormalNewsCardPages(Long userId, Integer page, Integer size) {
        List<Category> categories = categoryRepository.findAll();
        Map<String, List<NewsCardDTO>> newsCardDTOS = new HashMap<>();

        // 전체
        Page<News> allNewsPage = newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page, size));
        newsCardDTOS.put("ALL", getNewsCardDTOList(userId, allNewsPage));

        // 카테고리별
        for (Category c : categories) {
            Page<News> newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(c.getId(), PageRequest.of(page, size));
            newsCardDTOS.put(c.getName().toString(), getNewsCardDTOList(userId, newsPage));
        }
        return newsCardDTOS;
    }

    @Override
    public NewsDetailResponse getNewsDetail(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 뉴스를 찾을 수 없습니다."));

        Optional<User> user;
        if (userId != null) {
            user = userRepository.findById(userId);
        } else {
            user = Optional.empty();
        }

        List<TimelineCardDTO> timelineCardDTOList = getTimelineCardDTOList(news);

        Optional<NewsImage> newsImage = newsImageRepository.findByNewsId(news.getId());
        String image = newsImage.map(NewsImage::getUrl).orElse(null);

        StatisticsDTO statistics = getStatisticsDTO(news);
        boolean bookmarked = user.map(u -> getBookmarked(u, news)).orElse(false);

        news.setViewCount(news.getViewCount() + 1);
        newsRepository.save(news);

        return new NewsDetailResponse(
                news.getTitle(),
                image,
                news.getUpdatedAt(),
                bookmarked,
                timelineCardDTOList,
                statistics
        );
    }

    @Override
    @Transactional
    public NewsDetailResponse save(Long userId, boolean isHotissue, NewsCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 0. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = asyncAiService.getAIStatisticsDTO(STATISTIC_AI_ENDPOINT, req.getKeywords(), STATISTICS_AI_SEARCH_CNT);

        // 1. AI에 요청하여 뉴스를 생성한다.
        LocalDate endAt = LocalDate.now();
        LocalDate startAt = endAt.minusDays(NEWS_CREATE_DAYS);
        WrappedDTO<AINewsResponse> res = createAINews(req.getKeywords(), startAt, endAt);
        if (res == null || res.getData() == null) {
            return null;
        }
        AINewsResponse aiNewsResponse = res.getData();

        // 2. AI에 요청하여 타임라인 카드들을 병합한다.
        List<TimelineCardDTO> timeline = mergeTimelineCards(aiNewsResponse.getTimeline());

        // 3. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? statsAsync.join().getData() : null;

        // 4. 저장
        // 4-1. 뉴스를 저장한다.
        Category category = null;
        if (aiNewsResponse.getCategory() != null || !aiNewsResponse.getCategory().isEmpty() || !aiNewsResponse.getCategory().equals("")) {
            try {
                CategoryType categoryType = CategoryType.valueOf(aiNewsResponse.getCategory());
                category = categoryRepository.findByName(categoryType)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다."));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 카테고리 형식입니다.");
            }
        }

        News news = new News();
        news.setTitle(aiNewsResponse.getTitle());
        news.setSummary(aiNewsResponse.getSummary());
        news.setIsHotissue(isHotissue);
        if (statistics != null) {
            news.setRatioPosi(statistics.getPositive());
            news.setRatioNeut(statistics.getNeutral());
            news.setRatioNega(statistics.getNegative());
        }
        news.setUser(user);
        news.setCategory(category);
        newsRepository.save(news);

        // 4-2. 타임라인 카드들을 저장한다.
        saveTimelineCards(timeline, news);

        // 4-3. 뉴스 이미지를 저장한다.
        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl(aiNewsResponse.getImage());
        newsImageRepository.save(newsImage);

        // 5. 뉴스 태그들을 저장하고, DB에 없는 태그를 저장한다.
        req.getKeywords().forEach(keyword -> {
            NewsTag newsTag = new NewsTag();
            newsTag.setNews(news);

            Optional<Tag> tag = tagRepository.findByName(keyword);
            if (tag.isPresent()) {
                newsTag.setTag(tag.get());
                newsTagRepository.save(newsTag);
            } else {
                Tag newTag = new Tag();
                newTag.setName(keyword);
                tagRepository.save(newTag);

                newsTag.setTag(newTag);
                newsTagRepository.save(newsTag);
            }
        });

        // 6. 생성된 뉴스에 대해 북마크 설정한다.
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNews(news);
        bookmarkRepository.save(bookmark);

        // 7. 뉴스의 상세 페이지 데이터를 반환한다.
        return new NewsDetailResponse(
                news.getTitle(),
                newsImage.getUrl(),
                news.getUpdatedAt(),
                true,
                timeline,
                statistics
        );
    }

    @Override
    @Transactional
    public NewsDetailResponse update(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 1. 뉴스, 타임라인 카드들, 뉴스태그들을 찾는다.
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 뉴스를 찾을 수 없습니다."));

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusHours(NEWS_UPDATE_HOURS))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 업데이트 이후 24시간이 지나지 않았습니다.");
        }

        List<TimelineCard> timelineCards = timelineCardRepository.findAllByNewsIdAndDuration(newsId, null);
        List<TimelineCardDTO> oldTimeline = new ArrayList<>();
        for (TimelineCard tc : timelineCards) {
            TimelineCardDTO timelineCardDTO = new TimelineCardDTO(
                    tc.getTitle(),
                    tc.getContent(),
                    tc.getSource(),
                    tc.getDuration().toString(),
                    tc.getStartAt(),
                    tc.getEndAt()
            );
            oldTimeline.add(timelineCardDTO);
        }

        List<NewsTag> tags = newsTagRepository.findByNewsId(news.getId());
        List<String> keywords = new ArrayList<>();
        for (NewsTag tag : tags) {
            keywords.add(tag.getTag().getName());
        }

        // 2-1. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = asyncAiService.getAIStatisticsDTO(STATISTIC_AI_ENDPOINT, keywords, STATISTICS_AI_SEARCH_CNT);

        // 3. AI에게 요청하여 가장 최신 타임라인 카드의 endAt 이후 시점에 대한 뉴스를 생성한다.
        LocalDate startAt = timelineCards.getFirst().getEndAt();
        LocalDate endAt = LocalDate.now();
        WrappedDTO<AINewsResponse> res = createAINews(keywords, startAt, endAt);
        if (res == null || res.getData() == null) {
            return null;
        }
        AINewsResponse aiNewsResponse = res.getData();

        // 4. 기존 타임라인 카드들과 합친 뒤, AI에게 요청하여 타임라인 카드들을 병합한다.
        oldTimeline.addAll(aiNewsResponse.getTimeline());
        List<TimelineCardDTO> newTimeline = mergeTimelineCards(oldTimeline);

        // 2-2. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? statsAsync.join().getData() : null;

        // 4. 저장
        // 4-1. 뉴스를 저장한다.
        news.setSummary(aiNewsResponse.getSummary());

        news.setUpdateCount(news.getUpdateCount() + 1);
        if (statistics != null) {
            news.setRatioPosi(statistics.getPositive());
            news.setRatioNeut(statistics.getNeutral());
            news.setRatioNega(statistics.getNegative());
        }
        newsRepository.save(news);

        // 4-2. 타임라인 카드들을 저장한다.
        timelineCardRepository.deleteAllByNewsId(news.getId());
        saveTimelineCards(newTimeline, news);

        // 4-3. 기존 뉴스 이미지를 삭제하고 새로운 뉴스 이미지를 저장한다.
        if (newsImageRepository.findByNewsId(news.getId()).isPresent()) {
            Optional<NewsImage> oldNewsImage = newsImageRepository.findByNewsId(news.getId());
            newsImageRepository.delete(oldNewsImage.get());
        }
        NewsImage updatedNewsImage = new NewsImage();
        updatedNewsImage.setNews(news);
        updatedNewsImage.setUrl(aiNewsResponse.getImage());
        newsImageRepository.save(updatedNewsImage);

        // 5. 생성된 뉴스에 대해 북마크 설정한다.
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isEmpty()) {
            Bookmark savedBookmark = new Bookmark();
            savedBookmark.setUser(user);
            savedBookmark.setNews(news);
            bookmarkRepository.save(savedBookmark);
        }

        // 6. 뉴스의 상세 페이지 데이터를 반환한다.
        return new NewsDetailResponse(
                news.getTitle(),
                updatedNewsImage.getUrl(),
                news.getUpdatedAt(),
                true,
                newTimeline,
                statistics
        );
    }

    @Override
    public void delete(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "뉴스 삭제에 대한 권한이 없습니다.");
        }

        News news = newsRepository.findById(newsId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 뉴스를 찾을 수 없습니다."));

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(NEWS_DELETE_DAYS))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 업데이트 이후 3개월이 지나지 않았습니다.");
        }

        newsRepository.delete(news);
    }


    /*
        AI 통신용
     */

    private WrappedDTO<AINewsResponse> createAINews(List<String> keywords, LocalDate startAt, LocalDate endAt) {
        AINewsRequest aiNewsRequest = new AINewsRequest(
                keywords,
                startAt,
                endAt
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return aiWebClient.post()
                .uri(TIMELINE_AI_ENDPOINT)
                .bodyValue(aiNewsRequest)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NOT_FOUND,
                        clientResponse -> Mono.empty()
                )
                .onStatus(
                        HttpStatusCode::isError,
                        clientResponse -> clientResponse
                                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AINewsResponse>>() {})
                                .flatMap(errorBody -> Mono.error(new AIException(errorBody)))
                )
                .bodyToMono(new ParameterizedTypeReference<WrappedDTO<AINewsResponse>>() {})
                .block();
    }

    private List<TimelineCardDTO> mergeTimelineCards(List<TimelineCardDTO> timeline) {
        // 1. 1일카드 -> 1주카드
        timeline = mergeAITimelineCards(timeline, TimelineCardType.DAY, 7);

        // 2. 1주카드 -> 1달카드
        timeline = mergeAITimelineCards(timeline, TimelineCardType.WEEK, 4);

        // 3. 1달카드: 3개월 지남 -> 삭제
        timeline.removeIf(tc -> (TimelineCardType.valueOf(tc.getDuration()) == TimelineCardType.MONTH)
                && (tc.getStartAt().isBefore(LocalDate.now().minusMonths(3))));

        return timeline;
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
                        .uri(MERGE_AI_ENDPOINT)
                        .bodyValue(mergeRequest)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                clientResponse -> clientResponse
                                        .bodyToMono(new ParameterizedTypeReference<WrappedDTO<TimelineCardDTO>>() {})
                                        .flatMap(errorBody -> Mono.error(new AIException(errorBody)))
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


    /*
        함수 편의용
    */

    private StatisticsDTO getStatisticsDTO(News news) {
        return new StatisticsDTO(
                news.getRatioPosi(),
                news.getRatioNeut(),
                news.getRatioNega()
        );
    }

    private void saveTimelineCards (List<TimelineCardDTO> timeline, News news) {
        for (TimelineCardDTO dto : timeline) {
            TimelineCard tc = new TimelineCard();
            tc.setTitle(dto.getTitle());
            tc.setContent(dto.getContent());
            tc.setSource(dto.getSource());
            tc.setDuration(TimelineCardType.valueOf(dto.getDuration()));
            tc.setStartAt(dto.getStartAt());
            tc.setEndAt(dto.getEndAt());
            tc.setNews(news);
            timelineCardRepository.save(tc);
        }
    }

    private List<NewsCardDTO> getNewsCardDTOList(Long userId, Page<News> newsPage) {
        Optional<User> user;
        if (userId != null) {
            user = userRepository.findById(userId);
        } else {
            user = Optional.empty();
        }

        List<NewsCardDTO> newsCardDTOList = new ArrayList<>();

        newsPage.forEach(news -> {
            Optional<NewsImage> newsImage = newsImageRepository.findByNewsId(news.getId());
            String image = newsImage.map(NewsImage::getUrl).orElse(null);

            String categoryName = news.getCategory() != null ? news.getCategory().getName().toString() : null;

            boolean bookmarked = user.map(u -> getBookmarked(u, news)).orElse(false);
            LocalDateTime bookmarkedAt = user.map(u -> getBookmarkedAt(u, news)).orElse(null);


            NewsCardDTO dto = new NewsCardDTO(
                    news.getId(),
                    news.getTitle(),
                    news.getSummary(),
                    image,
                    categoryName,
                    news.getUpdatedAt(),
                    bookmarked,
                    bookmarkedAt
            );
            newsCardDTOList.add(dto);
        });

        return newsCardDTOList;
    }

    private List<TimelineCardDTO> getTimelineCardDTOList(News news) {
        List<TimelineCard> timeline = timelineCardRepository.findAllByNewsIdAndDuration(news.getId(), null);
        List<TimelineCardDTO> timelineCardDTOList = new ArrayList<>();
        timeline.forEach(tc -> {
            TimelineCardDTO dto = new TimelineCardDTO(
                    tc.getTitle(),
                    tc.getContent(),
                    tc.getSource(),
                    tc.getDuration().toString(),
                    tc.getStartAt(),
                    tc.getEndAt()
            );
            timelineCardDTOList.add(dto);
        });
        return timelineCardDTOList;
    }

    private boolean getBookmarked(User user, News news) {
        if (user == null) return false;
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        return bookmark.isPresent();
    }

    private LocalDateTime getBookmarkedAt(User user, News news) {
        if (user == null) return null;
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        return bookmark.map(Bookmark::getCreatedAt).orElse(null);
    }
}
