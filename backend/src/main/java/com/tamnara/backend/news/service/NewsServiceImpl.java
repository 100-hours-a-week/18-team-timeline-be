package com.tamnara.backend.news.service;

import com.tamnara.backend.alarm.constant.AlarmMessage;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.AIException;
import com.tamnara.backend.news.constant.NewsResponseMessage;
import com.tamnara.backend.news.constant.NewsServiceConstant;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.domain.NewsTag;
import com.tamnara.backend.news.domain.Tag;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.StatisticsDTO;
import com.tamnara.backend.news.dto.TimelineCardDTO;
import com.tamnara.backend.news.dto.request.KtbNewsCreateRequest;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.AIHotissueResponse;
import com.tamnara.backend.news.dto.response.AINewsResponse;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.NewsListResponse;
import com.tamnara.backend.news.dto.response.category.AllResponse;
import com.tamnara.backend.news.dto.response.category.EconomyResponse;
import com.tamnara.backend.news.dto.response.category.EntertainmentResponse;
import com.tamnara.backend.news.dto.response.category.KtbResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;
import com.tamnara.backend.news.dto.response.category.SportsResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final ApplicationEventPublisher eventPublisher;

    private final AiService aiService;

    private final NewsRepository newsRepository;
    private final TimelineCardRepository timelineCardRepository;
    private final NewsImageRepository newsImageRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NewsTagRepository newsTagRepository;

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public HotissueNewsListResponse getHotissueNewsCardPage() {
        log.info("[NEWS] getHotissueNewsCardPage 시작");

        Page<News> newsPage = newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged());
        List<NewsCardDTO> newsCardDTOList = getNewsCardDTOList(null, newsPage);
        log.info("[NEWS] getHotissueNewsCardPage 처리 중 - 핫이슈 조회 성공");

        log.info("[NEWS] getHotissueNewsCardPage 완료");
        return new HotissueNewsListResponse(newsCardDTOList);
    }

    @Override
    public MultiCategoryResponse getMultiCategoryPage(Long userId, Integer offset) {
        log.info("[NEWS] getMultiCategoryPage 시작");

        int page = offset / NewsServiceConstant.PAGE_SIZE;
        int nextOffset = (page + 1) * NewsServiceConstant.PAGE_SIZE;

        Page<News> newsPage;
        MultiCategoryResponse res = new MultiCategoryResponse();

        newsPage = newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setAll(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );
        log.info("[NEWS] getMultiCategoryPage 처리 중 - 뉴스 조회 성공, category:{}", "ALL");

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ECONOMY.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setEconomy(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ECONOMY.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );
        log.info("[NEWS] getMultiCategoryPage 처리 중 - 뉴스 조회 성공, category:{}", CategoryType.ECONOMY.name());

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ENTERTAINMENT.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setEntertainment(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ENTERTAINMENT.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );
        log.info("[NEWS] getMultiCategoryPage 처리 중 - 뉴스 조회 성공, category:{}", CategoryType.ENTERTAINMENT.name());

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.SPORTS.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setSports(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.SPORTS.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );
        log.info("[NEWS] getMultiCategoryPage 처리 중 - 뉴스 조회 성공, category:{}", CategoryType.SPORTS.name());

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.KTB.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setKtb(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.KTB.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );
        log.info("[NEWS] getMultiCategoryPage 처리 중 - 뉴스 조회 성공, category:{}", CategoryType.KTB.name());

        log.info("[NEWS] getMultiCategoryPage 완료");
        return res;
    }

    @Override
    public Object getSingleCategoryPage(Long userId, String category, Integer offset) {
        log.info("[NEWS] getSingleCategoryPage 시작");

        if (category != null && !category.equalsIgnoreCase("ALL")) {
            categoryRepository.findByName(CategoryType.valueOf(category.toUpperCase()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NewsResponseMessage.CATEGORY_NOT_FOUND));
        }
        log.info("[NEWS] getSingleCategoryPage 처리 중 - 카테고리 유효성 검사 성공");

        Integer page = offset / NewsServiceConstant.PAGE_SIZE;
        NewsListResponse newsListResponse = getNewsListResponse(userId, category, page);

        log.info("[NEWS] getSingleCategoryPage 완료");
        return switch (category != null ? category.toUpperCase() : "ALL") {
            case "ALL" -> new AllResponse(newsListResponse);
            case "ECONOMY" -> new EconomyResponse(newsListResponse);
            case "ENTERTAINMENT" -> new EntertainmentResponse(newsListResponse);
            case "SPORTS" -> new SportsResponse(newsListResponse);
            case "KTB" -> new KtbResponse(newsListResponse);
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public NewsListResponse getSearchNewsCardPage(Long userId, List<String> tags, Integer offset) {
        log.info("[NEWS] getSearchNewsCardPage 시작 - userId:{}", userId);

        tags = new ArrayList<>(new LinkedHashSet<>(tags));
        if (tags.size() < NewsServiceConstant.TAGS_MIN_SIZE || tags.size() > NewsServiceConstant.TAGS_MAX_SIZE) {
            log.error("[NEWS] getSearchNewsCardPage 실패 - 유효하지 않은 태그 개수, tagsSize:{} userId:{}", tags.size(), userId);
            throw new IllegalArgumentException();
        }

        int page = offset / NewsServiceConstant.PAGE_SIZE;
        int nextOffset = (page + 1) * NewsServiceConstant.PAGE_SIZE;
        Page<News> newsPage = newsRepository.searchNewsPageByTags(tags, PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        log.info("[NEWS] getSearchNewsCardPage 처리 중 - 뉴스 목록 검색 성공, userId:{}", userId);

        log.info("[NEWS] getSearchNewsCardPage 완료 - userId:{}", userId);
        return new NewsListResponse(
                getNewsCardDTOList(userId, newsPage),
                nextOffset,
                newsPage.hasNext()
        );
    }

    @Override
    @Transactional
    public NewsDetailDTO getNewsDetail(Long newsId, Long userId) {
        log.info("[NEWS] getNewsDetail 시작 - userId:{} newsId:{}", userId, newsId);

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[NEWS] getNewsDetail 처리 중 - 뉴스 조회 성공, userId:{} newsId:{}", userId, newsId);

        Optional<User> user;
        if (userId != null) {
            user = userRepository.findById(userId);
            log.info("[NEWS] getNewsDetail 처리 중 - 회원 조회 성공, userId:{} newsId:{}", userId, newsId);
        } else {
            user = Optional.empty();
            log.info("[NEWS] getNewsDetail 처리 중 - 비회원 확인, userId:{} newsId:{}", userId, newsId);
        }

        List<TimelineCardDTO> timelineCardDTOList = getTimelineCardDTOList(news);
        log.info("[NEWS] getNewsDetail 처리 중 - 뉴스 타임라인 조회 성공, userId:{} newsId:{}", userId, newsId);

        Optional<NewsImage> newsImage = newsImageRepository.findByNewsId(news.getId());
        String image = newsImage.map(NewsImage::getUrl).orElse(null);
        log.info("[NEWS] getNewsDetail 처리 중 - 뉴스 썸네일 조회 성공, userId:{} newsId:{}", userId, newsId);

        StatisticsDTO statistics = getStatisticsDTO(news);
        boolean bookmarked = user.map(u -> getBookmarked(u, news)).orElse(false);
        log.info("[NEWS] getNewsDetail 처리 중 - 뉴스 북마크 조회 성공, userId:{} newsId:{}", userId, newsId);

        newsRepository.increaseViewCount(news.getId());
        log.info("[NEWS] getNewsDetail 처리 중 - 뉴스 조회수 상승 처리 성공, userId:{} newsId:{}", userId, newsId);

        log.info("[NEWS] getNewsDetail 완료 - userId:{} newsId:{}", userId, newsId);
        return new NewsDetailDTO(
                news.getId(),
                news.getTitle(),
                image,
                news.getCategory().getName().toString(),
                news.getUpdatedAt(),
                bookmarked,
                timelineCardDTOList,
                statistics
        );
    }

    @Override
    @Transactional
    public NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req) {
        log.info("[NEWS] save 시작 - hotissue:{}, userId:{}", isHotissue, userId);

        User user = null;
        if (!isHotissue) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
            log.info("[NEWS] save 처리 중 - 회원 조회 성공, hotissue:{}, userId:{}", isHotissue, userId);
        }

        // 0. 뉴스 생성 키워드 목록과 기존 뉴스의 태그 목록이 일치할 경우, 기존 뉴스를 업데이트한다.
        Optional<News> optionalNews = newsRepository.findNewsByExactlyMatchingTags(req.getKeywords(), req.getKeywords().size());
        if (optionalNews.isPresent()) {
            log.info("[NEWS] save 완료 - 태그 목록이 일치하는 기존 뉴스가 존재함, hotissue:{}, userId:{}", isHotissue, userId);

            Long newsId = optionalNews.get().getId();
            return update(newsId, userId, isHotissue);
        }
        log.info("[NEWS] save 처리 중 - 태그 목록이 일치하는 기존 뉴스가 존재하지 않음, hotissue:{}, userId:{}", isHotissue, userId);

        // 1. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        log.info("[NEWS] save 처리 중 - 뉴스 여론 통계 비동기 생성 시작, hotissue:{}, userId:{}", isHotissue, userId);
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = aiService
                .getAIStatistics(req.getKeywords())
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AIException aiEx) {
                        HttpStatusCode status = aiEx.getStatus();
                        if (status.is4xxClientError()) {
                            log.warn("[NEWS] save 처리 중 - 뉴스 여론 통계 비동기 생성 실패, hotissue:{}, userId:{}", isHotissue, userId);
                            return null;
                        }
                    }
                    throw new CompletionException(cause);
                });

        // 2. AI에 요청하여 뉴스를 생성한다.
        AINewsResponse aiNewsResponse;
        try {
            log.info("[NEWS] save 처리 중 - 타임라인 생성 시작, hotissue:{}, userId:{}", isHotissue, userId);
            LocalDate endAt = LocalDate.now();
            LocalDate startAt = endAt.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS);
            WrappedDTO<AINewsResponse> res = aiService.createAINews(req.getKeywords(), startAt, endAt);
            aiNewsResponse = res.getData();
            log.info("[NEWS] save 처리 중 - 타임라인 생성 성공, hotissue:{}, userId:{}", isHotissue, userId);
        } catch (AIException e) {
            log.error("[NEWS] save 실패 - 타임라인 생성 실패, hotissue:{}, userId:{}", isHotissue, userId);
            if (e.getStatus() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }

        // 3. AI에 요청하여 타임라인 카드들을 병합한다.
        List<TimelineCardDTO> timeline = aiService.mergeTimelineCards(aiNewsResponse.getTimeline());
        log.info("[NEWS] save 처리 중 - 타임라인 카드 병합 성공, hotissue:{}, userId:{}", isHotissue, userId);

        // 4. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? resStats.getData() : null;
        log.info("[NEWS] save 처리 중 - 뉴스 여론 통계 비동기 처리 응답 반환, hotissue:{}, userId:{}", isHotissue, userId);

        // 5. 저장
        // 5-1. 뉴스를 저장한다.
        Category category = null;
        if (aiNewsResponse.getCategory() != null && !aiNewsResponse.getCategory().isBlank()) {
            try {
                CategoryType categoryType = CategoryType.valueOf(aiNewsResponse.getCategory());
                category = categoryRepository.findByName(categoryType).orElse(null);
            } catch (IllegalArgumentException ignored) {
                // 유효하지 않은 카테고리는 기타(null)로 처리한다.
            }
        }
        log.info("[NEWS] save 처리 중 - 카테고리 유효성 검사 완료, hotissue:{}, userId:{}", isHotissue, userId);

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
        log.info("[NEWS] save 처리 중 - 뉴스 저장 완료, hotissue:{}, userId:{}", isHotissue, userId);

        // 5-2. 타임라인 카드들을 저장한다.
        saveTimelineCards(timeline, news);
        log.info("[NEWS] save 처리 중 - 타임라인 카드 저장 완료, hotissue:{}, userId:{}", isHotissue, userId);

        // 5-3. 뉴스 이미지를 저장한다.
        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl(aiNewsResponse.getImage());
        newsImageRepository.save(newsImage);
        log.info("[NEWS] save 처리 중 - 뉴스 이미지 저장 완료, hotissue:{}, userId:{}", isHotissue, userId);

        // 5-4. 뉴스 태그들을 저장하고, DB에 없는 태그를 저장한다.
        log.info("[NEWS] save 처리 중 - 뉴스 태그 저장 시작, hotissue:{}, userId:{}", isHotissue, userId);
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
                log.info("[NEWS] save 처리 중 - 새로운 태그 저장 완료, hotissue:{}, userId:{}", isHotissue, userId);
            }
        });
        log.info("[NEWS] save 처리 중 - 뉴스 태그 저장 완료, hotissue:{}, userId:{}", isHotissue, userId);

        // 6. 생성된 뉴스에 대해 북마크 설정한다.
        if (!isHotissue) {
            Bookmark bookmark = new Bookmark();
            bookmark.setUser(user);
            bookmark.setNews(news);
            bookmarkRepository.save(bookmark);
            log.info("[NEWS] save 처리 중 - 북마크 처리 완료, hotissue:{}, userId:{}", isHotissue, userId);
        }

        // 7. 뉴스의 상세 페이지 데이터를 반환한다.
        log.info("[NEWS] save 완료 - hotissue:{}, userId:{}", isHotissue, userId);
        return new NewsDetailDTO(
                news.getId(),
                news.getTitle(),
                newsImage.getUrl(),
                news.getCategory().getName().toString(),
                news.getUpdatedAt(),
                true,
                timeline,
                statistics != null ? statistics : new StatisticsDTO(0, 0, 0)
        );
    }

    @Override
    @Transactional
    public NewsDetailDTO saveKtbNews(Long userId, KtbNewsCreateRequest req) {
        log.info("[NEWS] saveKtbNews 시작 - userId:{}", userId);

        User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[NEWS] saveKtbNews 처리 중 - 회원 조회 성공, userId:{}", userId);

        if (user.getRole() != Role.ADMIN) {
            log.error("[NEWS] saveKtbNews 실패 - 관리자가 아님, userId:{} userRole:{}", userId, user.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ResponseMessage.USER_FORBIDDEN);
        }
        log.info("[NEWS] saveKtbNews 처리 중 - 관리자 확인, userId:{}", userId);

        News news = new News();
        news.setTitle(req.getTitle());
        news.setSummary(req.getSummary());
        news.setIsPublic(false);
        news.setCategory(categoryRepository.findByName(CategoryType.KTB).orElse(null));
        newsRepository.save(news);
        log.info("[NEWS] saveKtbNews 처리 중 - 뉴스 저장 성공, userId:{}", userId);

        if (req.getImage() != null) {
            NewsImage newsImage = new NewsImage();
            newsImage.setNews(news);
            newsImage.setUrl(req.getImage());
            newsImageRepository.save(newsImage);
            log.info("[NEWS] saveKtbNews 처리 중 - 뉴스 이미지 저장 성공, userId:{}", userId);
        }

        for (TimelineCardDTO dto : req.getTimeline()) {
            TimelineCard timelineCard = new TimelineCard();
            timelineCard.setNews(news);
            timelineCard.setTitle(dto.getTitle());
            timelineCard.setContent(dto.getContent());
            timelineCard.setSource(dto.getSource());
            timelineCard.setDuration(TimelineCardType.DAY);
            timelineCard.setStartAt(dto.getStartAt());
            timelineCard.setEndAt(dto.getEndAt());
            timelineCardRepository.save(timelineCard);
        }
        log.info("[NEWS] saveKtbNews 처리 중 - 타임라인 카드 저장 성공, userId:{}", userId);

        log.info("[NEWS] saveKtbNews 완료 - userId:{}", userId);
        return new NewsDetailDTO(
          news.getId(),
          news.getTitle(),
          req.getImage(),
          news.getCategory().getName().toString(),
          news.getUpdatedAt(),
          false,
          req.getTimeline(),
          getStatisticsDTO(news)
        );
    }

    @Override
    @Transactional
    public NewsDetailDTO update(Long newsId, Long userId, boolean isHotissue) {
        log.info("[NEWS] update 시작 - hotissue:{} userId:{}", isHotissue, userId);

        User user = null;
        if (!isHotissue) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
            log.info("[NEWS] update 처리 중 - 회원 조회 성공, hotissue:{} userId:{}", isHotissue, userId);
        }

        // 1. 뉴스, 타임라인 카드들, 뉴스태그들을 찾는다.
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[NEWS] update 처리 중 - 기존 뉴스 조회 성공, hotissue:{} userId:{}", isHotissue, userId);

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS))) {
            if (isHotissue) {
                news.setIsHotissue(true);
                newsRepository.save(news);
                log.info("[NEWS] update 처리 중 - 기존 뉴스 핫이슈 전환 성공, hotissue:{} userId:{}", isHotissue, userId);

                log.info("[NEWS] update 완료 - hotissue:{} userId:{}", isHotissue, userId);
                return new NewsDetailDTO(
                        news.getId(),
                        news.getTitle(),
                        news.getSummary(),
                        news.getCategory().getName().toString(),
                        news.getUpdatedAt(),
                        false,
                        getTimelineCardDTOList(news),
                        getStatisticsDTO(news)
                );
            }
            log.error("[NEWS] update 실패 - 업데이트 가능 시간이 아님, hotissue:{} userId:{}", isHotissue, userId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, NewsResponseMessage.NEWS_UPDATE_CONFLICT);
        }

        List<TimelineCard> timelineCards = timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(newsId);
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
        log.info("[NEWS] update 처리 중 - 타임라인 카드 목록 조회 성공, hotissue:{} userId:{}", isHotissue, userId);

        List<NewsTag> tags = newsTagRepository.findByNewsId(news.getId());
        List<String> keywords = new ArrayList<>();
        for (NewsTag tag : tags) {
            keywords.add(tag.getTag().getName());
        }
        log.info("[NEWS] update 처리 중 - 뉴스 태그 목록 조회 성공 hotissue:{} userId:{}", isHotissue, userId);

        // 2-1. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        log.info("[NEWS] update 처리 중 - 뉴스 여론 통계 비동기 생성 시작, hotissue:{} userId:{}", isHotissue, userId);
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = aiService
                .getAIStatistics(keywords)
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AIException aiEx) {
                        HttpStatusCode status = aiEx.getStatus();
                        if (status.is4xxClientError()) {
                            log.warn("[NEWS] update 처리 중 - 뉴스 여론 통계 비동기 생성 실패, hotissue:{}, userId:{}", isHotissue, userId);
                            return null;
                        }
                    }
                    throw new CompletionException(cause);
                });

        // 3. AI에 요청하여 가장 최신 타임라인 카드의 endAt 이후 시점에 대한 뉴스를 생성한다.
        AINewsResponse aiNewsResponse;
        try {
            LocalDate startAt = timelineCards.getFirst().getEndAt().plusDays(1);
            LocalDate endAt = LocalDate.now();
            WrappedDTO<AINewsResponse> res = aiService.createAINews(keywords, startAt, endAt);
            aiNewsResponse = res.getData();
        } catch (AIException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                log.warn("[NEWS] update 실패 - 뉴스 생성 실패, hotissue:{} userId:{}", isHotissue, userId);
                return null;
            }
            throw ex;
        }
        log.info("[NEWS] update 처리 중 - 뉴스 생성 성공, hotissue:{} userId:{}", isHotissue, userId);

        // 4. 기존 타임라인 카드들과 합친 뒤, AI에 요청하여 타임라인 카드들을 병합한다.
        oldTimeline.addAll(aiNewsResponse.getTimeline());
        List<TimelineCardDTO> newTimeline = aiService.mergeTimelineCards(oldTimeline);
        log.info("[NEWS] update 처리 중 - 타임라인 카드 병합 성공, hotissue:{} userId:{}", isHotissue, userId);

        // 2-2. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? resStats.getData() : null;
        log.info("[NEWS] update 처리 중 - 뉴스 여론 통계 비동기 생성 응답 반환, hotissue:{} userId:{}", isHotissue, userId);

        // 4. 저장
        // 4-1. 뉴스를 저장한다.
        news.setSummary(aiNewsResponse.getSummary());
        news.setIsHotissue(isHotissue);

        news.setUpdateCount(news.getUpdateCount() + 1);
        if (statistics != null) {
            news.setRatioPosi(statistics.getPositive());
            news.setRatioNeut(statistics.getNeutral());
            news.setRatioNega(statistics.getNegative());
        }
        newsRepository.save(news);
        log.info("[NEWS] update 처리 중 - 뉴스 저장 완료, hotissue:{} userId:{}", isHotissue, userId);

        // 4-2. 타임라인 카드들을 저장한다.
        timelineCardRepository.deleteAllByNewsId(news.getId());
        saveTimelineCards(newTimeline, news);
        log.info("[NEWS] update 처리 중 - 타임라인 카드 저장 완료, hotissue:{} userId:{}", isHotissue, userId);

        // 4-3. 기존 뉴스 이미지를 삭제하고 새로운 뉴스 이미지를 저장한다.
        if (newsImageRepository.findByNewsId(news.getId()).isPresent()) {
            Optional<NewsImage> oldNewsImage = newsImageRepository.findByNewsId(news.getId());
            oldNewsImage.ifPresent(newsImageRepository::delete);
            log.info("[NEWS] update 처리 중 - 기존 뉴스 이미지 삭제, hotissue:{} userId:{}", isHotissue, userId);
        }
        NewsImage updatedNewsImage = new NewsImage();
        updatedNewsImage.setNews(news);
        updatedNewsImage.setUrl(aiNewsResponse.getImage());
        newsImageRepository.save(updatedNewsImage);
        log.info("[NEWS] update 처리 중 - 새로운 뉴스 이미지 저장 완료, hotissue:{} userId:{}", isHotissue, userId);

        // 5. 기존에 북마크를 설정했던 회원들에게 알림 생성
        publishAlarm(
                bookmarkRepository.findUsersByNews(news),
                AlarmMessage.BOOKMARK_UPDATE_TITLE,
                String.format(AlarmMessage.BOOKMARK_UPDATE_CONTENT, news.getTitle()),
                AlarmType.NEWS,
                newsId
        );
        log.info("[NEWS] update 처리 중 - 뉴스를 북마크한 회원에게 업데이트 알림 발행, hotissue:{} userId:{}", isHotissue, userId);

        // 6. 생성된 뉴스에 대해 북마크 설정한다.
        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isEmpty()) {
            Bookmark savedBookmark = new Bookmark();
            savedBookmark.setUser(user);
            savedBookmark.setNews(news);
            bookmarkRepository.save(savedBookmark);
        }
        log.info("[NEWS] update 처리 중 - hotissue:{} userId:{}", isHotissue, userId);

        // 7. 뉴스의 상세 페이지 데이터를 반환한다.
        return new NewsDetailDTO(
                news.getId(),
                news.getTitle(),
                updatedNewsImage.getUrl(),
                news.getCategory().getName().toString(),
                news.getUpdatedAt(),
                true,
                newTimeline,
                statistics != null ? statistics : new StatisticsDTO(0, 0, 0)
        );
    }

    @Override
    public void delete(Long newsId, Long userId) {
        log.info("[NEWS] delete 시작 - newsId:{}", newsId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[NEWS] delete 처리 중 - 회원 조회 성공, newsId:{} userId:{}", newsId, userId);


        if (user.getRole() != Role.ADMIN) {
            log.error("[NEWS] delete 처리 중 - 관리자가 아님, userId:{} userRole:{}", userId,  user.getRole());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, NewsResponseMessage.NEWS_DELETE_FORBIDDEN);
        }
        log.info("[NEWS] delete 처리 중 - 관리자 권한 확인, newsId:{} userId:{}", newsId, userId);


        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));
        log.info("[NEWS] delete 처리 중 - 뉴스 조회 성공, newsId:{} userId:{}", newsId, userId);

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(NewsServiceConstant.NEWS_DELETE_DAYS))) {
            log.error("[NEWS] delete 실패 - 뉴스 삭제 조건이 충족되지 않음, newsId:{} userId:{}", newsId, userId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, NewsResponseMessage.NEWS_DELETE_CONFLICT);
        }


        publishAlarm(
                bookmarkRepository.findUsersByNews(news),
                AlarmMessage.BOOKMARK_DELETION_TITLE,
                String.format(AlarmMessage.BOOKMARK_DELETION_CONTENT, news.getTitle()),
                null,
                null
        );
        log.info("[NEWS] delete 처리 중 - 삭제 알림 발행, newsId:{} userId:{}", newsId, userId);

        log.info("[NEWS] delete 완료 - newsId:{} userId:{}", newsId, userId);
        newsRepository.delete(news);
    }

    @Override
    @Transactional
    public void createHotissueNews() {
        log.info("[NEWS] createHotissueNews 시작");

        AIHotissueResponse aiHotissueResponse;
        WrappedDTO<AIHotissueResponse> res = aiService.createAIHotissueKeywords();
        aiHotissueResponse = res.getData();

        List<News> previousHotissuesList = newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged()).getContent();
        for (News news : previousHotissuesList) {
            newsRepository.updateIsHotissue(news.getId(), false);
        }
        log.info("[NEWS] createHotissueNews 처리 중 - 이전 핫이슈 뉴스들을 일반 뉴스로 전환 성공");

        for (String keyword : aiHotissueResponse.getKeywords()) {
            NewsCreateRequest req = new NewsCreateRequest(List.of(keyword));
            save(null, true, req);
        }
        log.info("[NEWS] createHotissueNews 처리 중 - 새로운 핫이슈 뉴스 생성");

        publishAlarm(
                userRepository.findAll().stream().map(User::getId).collect(Collectors.toList()),
                AlarmMessage.HOTISSUE_CREATE_TITLE,
                AlarmMessage.HOTISSUE_CREATE_CONTENT,
                AlarmType.NEWS,
                null
        );
        log.info("[NEWS] createHotissueNews 처리 중 - 핫이슈 뉴스 생성 알림 생성 성공");

        log.info("[NEWS] createHotissueNews 완료");
    }

    @Override
    public void deleteOldNewsAndOrphanTags() {
        log.info("[NEWS] deleteOldNewsAndOrphanTags 시작");

        LocalDateTime cutoff = LocalDateTime.now().minusDays(NewsServiceConstant.NEWS_DELETE_DAYS);

        // 삭제 예정
        List<News> newsWarningList = newsRepository.findAllOlderThan(cutoff.plusDays(1));
        for (News news : newsWarningList) {
            publishAlarm(
                    bookmarkRepository.findUsersByNews(news),
                    AlarmMessage.BOOKMARK_DELETE_WARNING_TITLE,
                    String.format(AlarmMessage.BOOKMARK_DELETE_WARNING_CONTENT, news.getTitle()),
                    AlarmType.NEWS,
                    news.getId()
            );
        }
        log.info("[NEWS] deleteOldNewsAndOrphanTags 처리 중 - 삭제 예정 뉴스 알림 발행");

        // 삭제
        List<News> newsDeletionList = newsRepository.findAllOlderThan(cutoff);
        for (News news : newsDeletionList) {
            publishAlarm(
                    bookmarkRepository.findUsersByNews(news),
                    AlarmMessage.BOOKMARK_DELETION_TITLE,
                    String.format(AlarmMessage.BOOKMARK_DELETION_CONTENT, news.getTitle()),
                    null,
                    null
            );
        }
        log.info("[NEWS] deleteOldNewsAndOrphanTags 처리 중 - 삭제 대상 뉴스 알림 발행");

        newsRepository.deleteAllOlderThan(cutoff);
        log.info("[NEWS] deleteOldNewsAndOrphanTags 처리 중 - 뉴스 삭제 성공");

        tagRepository.deleteAllOrphan();
        log.info("[NEWS] deleteOldNewsAndOrphanTags 처리 중 - 고아 태그 삭제 성공");

        log.info("[NEWS] deleteOldNewsAndOrphanTags 완료");
    }

    @Override
    @Transactional
    public void makeNewsPublic() {
        log.info("[NEWS] makeNewsPublic 시작");

        List<News> newsList = newsRepository.findAllByIsPublicFalseOrderByUpdatedAtDesc();
        for (News news : newsList) {
            news.setIsPublic(true);
            newsRepository.save(news);
        }
        log.info("[NEWS] makeNewsPublic 처리 중 - 뉴스 공개 전환 성공");

        if (!newsList.isEmpty()) {
            publishAlarm(
                    userRepository.findAll().stream().map(User::getId).collect(Collectors.toList()),
                    AlarmMessage.POLL_RESULT_TITLE,
                    String.format(AlarmMessage.POLL_RESULT_CONTENT, newsList.getFirst().getTitle()),
                    AlarmType.NEWS,
                    newsList.getFirst().getId()
            );
            log.info("[NEWS] makeNewsPublic 처리 중 - KTB 뉴스 생성 알림 발행");
        }

        log.info("[NEWS] makeNewsPublic 완료");
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

    private NewsListResponse getNewsListResponse(Long userId, String category, Integer page) {
        Long categoryId = null;
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            Category c = categoryRepository.findByName(CategoryType.valueOf(category))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NewsResponseMessage.CATEGORY_NOT_FOUND));
            categoryId = c.getId();
        }

        int nextOffset = (page + 1) * NewsServiceConstant.PAGE_SIZE;
        NewsListResponse newsListResponse;

        if (category == null || category.equalsIgnoreCase("ALL")) {
            Page<News> newsPage = newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
            List<NewsCardDTO> newsCardDTOList = getNewsCardDTOList(userId, newsPage);
            boolean hasNext = !newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty();

            newsListResponse = new NewsListResponse(newsCardDTOList, nextOffset, hasNext);
        } else {
            Page<News> newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(categoryId, PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
            List<NewsCardDTO> newsCardDTOList = getNewsCardDTOList(userId, newsPage);
            boolean hasNext = !newsRepository.findByIsHotissueFalseAndCategoryId(categoryId, PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty();

            newsListResponse = new NewsListResponse(newsCardDTOList, nextOffset, hasNext);
        }
        return newsListResponse;
    }

    private List<TimelineCardDTO> getTimelineCardDTOList(News news) {
        List<TimelineCard> timeline = timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId());
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

    private Long getCategoryId(String category) {
        Long categoryId = null;
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            Category c = categoryRepository.findByName(CategoryType.valueOf(category.toUpperCase()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NewsResponseMessage.CATEGORY_NOT_FOUND));
            categoryId = c.getId();
        }
        return categoryId;
    }

    private void publishAlarm(List<Long> userIdList, String title, String content, AlarmType targetType, Long targetId) {
        AlarmEvent event = new AlarmEvent(
                userIdList,
                title,
                content,
                targetType,
                targetId
        );
        eventPublisher.publishEvent(event);
    }
}
