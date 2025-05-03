package com.tamnara.backend.news.service;

import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
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
import com.tamnara.backend.news.dto.request.AINewsRequest;
import com.tamnara.backend.news.dto.request.AIStatisticsRequest;
import com.tamnara.backend.news.dto.request.AITimelineMergeReqeust;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final WebClient aiWebClient;

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
    private final String HOTISSUE_AI_ENDPOINT = "/hot";
    private final String STATISTIC_AI_ENDPOINT = "/comment";

    @Override
    public List<NewsCardDTO> getNewsCardPage(Long userId, boolean isHotissue, Integer page, Integer size) {
        Page<News> newsPage = newsRepository.findAllByIsHotissue(isHotissue, PageRequest.of(page, size));
        return getNewsCardDTOList(userId, newsPage);
    }

    @Override
    public List<NewsCardDTO> getNewsCardPage(Long userId, boolean isHotissue, String category, Integer page, Integer size) {
        Category c = categoryRepository.findByName(CategoryType.valueOf(category.toUpperCase()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."));

        Page<News> newsPage = newsRepository.findNewsByIsHotissueAndCategoryId(isHotissue, c.getId(), PageRequest.of(page, size));
        return getNewsCardDTOList(userId, newsPage);
    }

    @Override
    public Map<String, List<NewsCardDTO>> getNormalNewsCardPages(Long userId, boolean isHotissue, Integer page, Integer size) {
        List<Category> categories = categoryRepository.findAll();
        Map<String, List<NewsCardDTO>> newsCardDTOS = new HashMap<>();

        // 전체
        Page<News> allNewsPage = newsRepository.findAllByIsHotissue(isHotissue, PageRequest.of(page, size));
        newsCardDTOS.put("ALL", getNewsCardDTOList(userId, allNewsPage));

        // 카테고리별
        for (Category c : categories) {
            Page<News> newsPage = newsRepository.findNewsByIsHotissueAndCategoryId(isHotissue, c.getId(), PageRequest.of(page, size));
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
        StatisticsDTO statistics = getStatisticsDTO(news);
        boolean bookmarked = user.map(u -> getBookmarked(u, news)).orElse(false);

        return new NewsDetailResponse(
                news.getTitle(),
                news.getUpdatedAt(),
                bookmarked,
                timelineCardDTOList,
                statistics
        );
    }

    @Override
    public NewsDetailResponse save(Long userId, boolean isHotissue, NewsCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 1. AI에 요청하여 뉴스를 생성한다.
        LocalDate endAt = LocalDate.now();
        LocalDate startAt = endAt.minusMonths(3);
        AINewsResponse aiNewsResponse = createAINews(req.getKeywords(), startAt, endAt);

        // 2. AI에 요청하여 타임라인 카드들을 병합한다.
        List<TimelineCardDTO> timeline = mergeAITimelineCards(aiNewsResponse.getTimeline());

        // 3. AI에 요청하여 뉴스의 여론 통계를 생성한다.
        StatisticsDTO statistics = getAIStatisticsDTO(req.getKeywords(), 100);

        // 4. 저장
        // 4-1. 뉴스를 저장한다.
        Optional<Category> category = categoryRepository.findByName(CategoryType.valueOf(aiNewsResponse.getCategory()));

        News news = new News();
        news.setTitle(aiNewsResponse.getTitle());
        news.setSummary(aiNewsResponse.getSummary());
        news.setIsHotissue(isHotissue);
        news.setRatioPosi(statistics.getPositive());
        news.setRatioNeut(statistics.getNegative());
        news.setRatioNeut(statistics.getNegative());
        news.setUser(user);
        news.setCategory(category.get());
        newsRepository.save(news);

        // 4-2. 타임라인 카드들을 저장한다.
        saveTimelineCards(timeline, startAt, endAt, news);

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
                news.getUpdatedAt(),
                true,
                timeline,
                statistics
        );
    }

    @Override
    public NewsDetailResponse update(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        // 1. 뉴스, 타임라인 카드들, 뉴스태그들을 찾는다.
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 리소스를 찾을 수 없습니다."));

        if (news.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(1))) {
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

        // 2. AI에게 요청하여 가장 최신 타임라인 카드의 startAt 이후 시점에 대한 뉴스를 생성한다.
        LocalDate endAt = LocalDate.now();
        LocalDate startAt = endAt.minusMonths(3);
        AINewsResponse aiNewsResponse = createAINews(keywords, startAt, endAt);

        // 3. 기존 타임라인 카드들과 합친 뒤, AI에게 요청하여 타임라인 카드들을 병합한다.
        oldTimeline.addAll(aiNewsResponse.getTimeline());
        List<TimelineCardDTO> newTimeline = mergeAITimelineCards(oldTimeline);

        // 4. AI에 요청하여 뉴스의 여론 통계를 생성한다.
        StatisticsDTO statistics = getAIStatisticsDTO(keywords, 100);

        // 4. 저장
        // 4-1. 뉴스를 저장한다.
        news.setSummary(aiNewsResponse.getSummary());
        news.setViewCount(news.getViewCount() + 1);
        news.setUpdateCount(news.getUpdateCount() + 1);
        news.setRatioPosi(statistics.getPositive());
        news.setRatioNeut(statistics.getNegative());
        news.setRatioNega(statistics.getNegative());
        newsRepository.save(news);

        // 4-2. 타임라인 카드들을 저장한다.
        timelineCardRepository.deleteAllByNewsId(news.getId());
        saveTimelineCards(newTimeline, startAt, endAt, news);

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
                news.getUpdatedAt(),
                true,
                newTimeline,
                statistics
        );
    }

    @Override
    public Long delete(Long newsId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));
        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "뉴스 삭제에 대한 권한이 없습니다.");
        }

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "요청하신 뉴스를 찾을 수 없습니다."));

        if (!news.getUpdatedAt().isBefore(LocalDateTime.now().minusMonths(3))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "마지막 업데이트 이후 3개월이 지나지 않았습니다.");
        }

        newsRepository.delete(news);
        return newsId;
    }


    /*
        AI 통신용
     */

    private AINewsResponse createAINews(List<String> keywords, LocalDate startAt, LocalDate endAt) {
        AINewsRequest aiNewsRequest = new AINewsRequest(
                keywords,
                startAt,
                endAt
        );

        return aiWebClient.post()
                .uri(TIMELINE_AI_ENDPOINT)
                .bodyValue(aiNewsRequest)
                .retrieve()
                .bodyToMono(AINewsResponse.class)
                .block();
    }

    private List<TimelineCardDTO> mergeAITimelineCards(List<TimelineCardDTO> timeline) {
        timeline.sort(Comparator.comparing(TimelineCardDTO::getStartAt));

        // 1. 1일카드 -> 1주카드
        timeline = mergeTimelineCards(timeline, TimelineCardType.DAY, 7);

        // 2. 1주카드 -> 1달카드
        timeline = mergeTimelineCards(timeline, TimelineCardType.WEEK, 4);

        // 3. 1달카드: 3개월 지남 -> 삭제
        timeline.removeIf(tc -> (TimelineCardType.valueOf(tc.getDuration()) == TimelineCardType.MONTH)
                && (tc.getStartAt().isAfter(LocalDate.now().minusMonths(3))));

        return timeline;
    }

    private List<TimelineCardDTO> mergeTimelineCards(List<TimelineCardDTO> timeline, TimelineCardType duration, Integer countNum) {
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
                AITimelineMergeReqeust mergeRequest = new AITimelineMergeReqeust(temp);
                TimelineCardDTO merged = aiWebClient.post()
                        .uri(MERGE_AI_ENDPOINT)
                        .bodyValue(mergeRequest)
                        .retrieve()
                        .bodyToMono(TimelineCardDTO.class)
                        .block();

                mergedList.add(merged);

                temp.clear();
                count = 0;
            }
        }

        mergedList.addAll(temp);
        temp.clear();

        timeline = mergedList;
        timeline.sort(Comparator.comparing(TimelineCardDTO::getStartAt));

        return timeline;
    }

    private StatisticsDTO getAIStatisticsDTO(List<String> keywords, Integer num) {
        AIStatisticsRequest aiStatisticsRequest = new AIStatisticsRequest(
                keywords,
                num
        );

        return aiWebClient.post()
                .uri(STATISTIC_AI_ENDPOINT)
                .bodyValue(aiStatisticsRequest)
                .retrieve()
                .bodyToMono(StatisticsDTO.class)
                .block();
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

    private void saveTimelineCards (List<TimelineCardDTO> timeline, LocalDate startAt, LocalDate endAt, News news) {
        for (TimelineCardDTO timelineCardDTO : timeline) {
            TimelineCard tc = new TimelineCard();
            tc.setTitle(timelineCardDTO.getTitle());
            tc.setContent(timelineCardDTO.getContent());
            tc.setSource(timelineCardDTO.getSource());
            tc.setDuration(TimelineCardType.valueOf(timelineCardDTO.getDuration()));
            tc.setStartAt(startAt);
            tc.setEndAt(endAt);
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
            Optional<NewsImage> image = newsImageRepository.findByNewsId(news.getId());
            String imageUrl = image.map(NewsImage::getUrl).orElse(null);

            String categoryName = news.getCategory() != null ? news.getCategory().getName().toString() : null;

            boolean bookmarked = user.map(u -> getBookmarked(u, news)).orElse(false);
            LocalDateTime bookmarkedAt = user.map(u -> getBookmarkedAt(u, news)).orElse(null);


            NewsCardDTO dto = new NewsCardDTO(
                    news.getId(),
                    news.getTitle(),
                    news.getSummary(),
                    imageUrl,
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
        timeline.forEach(t -> {
            TimelineCardDTO dto = new TimelineCardDTO(
                    t.getTitle(),
                    t.getContent(),
                    t.getSource(),
                    t.getDuration().toString(),
                    t.getStartAt(),
                    t.getEndAt()
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
