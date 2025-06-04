package com.tamnara.backend.news.service;

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
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final AIService aiService;
    private final AsyncAIService asyncAiService;

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
        Page<News> newsPage = newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged());
        List<NewsCardDTO> newsCardDTOList = getNewsCardDTOList(null, newsPage);
        return new HotissueNewsListResponse(newsCardDTOList);
    }

    @Override
    public MultiCategoryResponse getMultiCategoryPage(Long userId, Integer offset) {
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

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ECONOMY.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setEconomy(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ECONOMY.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ENTERTAINMENT.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setEntertainment(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.ENTERTAINMENT.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.SPORTS.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setSports(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.SPORTS.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );

        newsPage = newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.KTB.name()), PageRequest.of(page, NewsServiceConstant.PAGE_SIZE));
        res.setKtb(
                new NewsListResponse(
                        getNewsCardDTOList(userId, newsPage),
                        nextOffset,
                        !newsRepository.findByIsHotissueFalseAndCategoryId(getCategoryId(CategoryType.KTB.name()), PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)).isEmpty()
                )
        );

        return res;
    }

    @Override
    public Object getSingleCategoryPage(Long userId, String category, Integer offset) {
        if (category != null && !category.equalsIgnoreCase("ALL")) {
            categoryRepository.findByName(CategoryType.valueOf(category.toUpperCase()))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NewsResponseMessage.CATEGORY_NOT_FOUND));
        }

        Integer page = offset / NewsServiceConstant.PAGE_SIZE;
        NewsListResponse newsListResponse = getNewsListResponse(userId, category, page);

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
    public NewsDetailDTO getNewsDetail(Long newsId, Long userId) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

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

        return new NewsDetailDTO(
                news.getId(),
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
    public NewsDetailDTO save(Long userId, boolean isHotissue, NewsCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        // 0. 뉴스 생성 키워드 목록과 기존 뉴스의 태그 목록이 일치할 경우, 기존 뉴스를 업데이트한다.
        Optional<News> optionalNews = newsRepository.findNewsByExactlyMatchingTags(req.getKeywords(), req.getKeywords().size());
        if (optionalNews.isPresent()) {
            Long newsId = optionalNews.get().getId();
            return update(newsId, userId);
        }

        // 1. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = asyncAiService
                .getAIStatistics(req.getKeywords())
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AIException aiEx && aiEx.getStatus() == HttpStatus.NOT_FOUND) {
                        return null;
                    }
                    throw new CompletionException(cause);
                });

        // 2. AI에 요청하여 뉴스를 생성한다.
        AINewsResponse aiNewsResponse;
        try {
            LocalDate endAt = LocalDate.now();
            LocalDate startAt = endAt.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS);
            WrappedDTO<AINewsResponse> res = aiService.createAINews(req.getKeywords(), startAt, endAt);
            aiNewsResponse = res.getData();
        } catch (AIException ex) {
            if (ex.getStatus() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw ex;
        }

        // 3. AI에 요청하여 타임라인 카드들을 병합한다.
        List<TimelineCardDTO> timeline = aiService.mergeTimelineCards(aiNewsResponse.getTimeline());

        // 4. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? resStats.getData() : null;

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

        // 5-2. 타임라인 카드들을 저장한다.
        saveTimelineCards(timeline, news);

        // 5-3. 뉴스 이미지를 저장한다.
        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl(aiNewsResponse.getImage());
        newsImageRepository.save(newsImage);

        // 5-4. 뉴스 태그들을 저장하고, DB에 없는 태그를 저장한다.
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
        return new NewsDetailDTO(
                news.getId(),
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
    public NewsDetailDTO update(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        // 1. 뉴스, 타임라인 카드들, 뉴스태그들을 찾는다.
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS))) {
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

        List<NewsTag> tags = newsTagRepository.findByNewsId(news.getId());
        List<String> keywords = new ArrayList<>();
        for (NewsTag tag : tags) {
            keywords.add(tag.getTag().getName());
        }

        // 2-1. 뉴스의 여론 통계 생성을 비동기적으로 시작한다.
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAsync = asyncAiService
                .getAIStatistics(keywords)
                .exceptionally(ex -> {
                    Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
                    if (cause instanceof AIException aiEx && aiEx.getStatus() == HttpStatus.NOT_FOUND) {
                        return null;
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
                return null;
            }
            throw ex;
        }

        // 4. 기존 타임라인 카드들과 합친 뒤, AI에 요청하여 타임라인 카드들을 병합한다.
        oldTimeline.addAll(aiNewsResponse.getTimeline());
        List<TimelineCardDTO> newTimeline = aiService.mergeTimelineCards(oldTimeline);

        // 2-2. 뉴스의 여론 통계 생성 응답을 기다린다.
        WrappedDTO<StatisticsDTO> resStats = statsAsync.join();
        StatisticsDTO statistics = (resStats != null && resStats.getData() != null) ? resStats.getData() : null;

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
            oldNewsImage.ifPresent(newsImageRepository::delete);
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
        return new NewsDetailDTO(
                news.getId(),
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, NewsResponseMessage.NEWS_DELETE_FORBIDDEN);
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(NewsServiceConstant.NEWS_DELETE_DAYS))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, NewsResponseMessage.NEWS_DELETE_CONFLICT);
        }

        newsRepository.delete(news);
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
}
