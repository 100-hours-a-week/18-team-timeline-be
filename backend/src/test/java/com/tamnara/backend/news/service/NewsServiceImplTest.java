package com.tamnara.backend.news.service;

import com.tamnara.backend.bookmark.repository.BookmarkRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock private AIService aiService;
    @Mock private AsyncAIService asyncAiService;

    @Mock private NewsRepository newsRepository;
    @Mock private TimelineCardRepository timelineCardRepository;
    @Mock private NewsImageRepository newsImageRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private NewsTagRepository newsTagRepository;

    @Mock private UserRepository userRepository;
    @Mock private BookmarkRepository bookmarkRepository;

    @InjectMocks private NewsServiceImpl newsServiceImpl;

    User user;
    User admin;
    Category economy;
    Category entertainment;
    Category sports;
    Category ktb;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getRole()).thenReturn(Role.USER);

        admin = mock(User.class);
        lenient().when(admin.getId()).thenReturn(2L);
        lenient().when(admin.getRole()).thenReturn(Role.ADMIN);

        economy = mock(Category.class);
        lenient().when(economy.getName()).thenReturn(CategoryType.ECONOMY);
        lenient().when(economy.getId()).thenReturn(1L);
        lenient().when(categoryRepository.findByName(CategoryType.ECONOMY)).thenReturn(Optional.of(economy));

        entertainment = mock(Category.class);
        lenient().when(entertainment.getName()).thenReturn(CategoryType.ENTERTAINMENT);
        lenient().when(entertainment.getId()).thenReturn(2L);
        lenient().when(categoryRepository.findByName(CategoryType.ENTERTAINMENT)).thenReturn(Optional.of(entertainment));

        sports = mock(Category.class);
        lenient().when(sports.getName()).thenReturn(CategoryType.SPORTS);
        lenient().when(sports.getId()).thenReturn(3L);
        lenient().when(categoryRepository.findByName(CategoryType.SPORTS)).thenReturn(Optional.of(sports));

        ktb = mock(Category.class);
        lenient().when(ktb.getName()).thenReturn(CategoryType.KTB);
        lenient().when(ktb.getId()).thenReturn(4L);
        lenient().when(categoryRepository.findByName(CategoryType.KTB)).thenReturn(Optional.of(ktb));
    }

    private News createNews(Long id, String title, String summary, Boolean isHotissue, User user, Category category) {
        News news = new News();
        news.setId(id);
        news.setTitle(title);
        news.setSummary(summary);
        news.setIsHotissue(isHotissue);
        news.setUser(user);
        news.setCategory(category);
        return news;
    }

    private TimelineCard createTimelineCard(News news, String title, String content, List<String> source, String duration, LocalDate startAt, LocalDate endAt) {
        TimelineCard timelineCard = new TimelineCard();
        timelineCard.setNews(news);
        timelineCard.setTitle(title);
        timelineCard.setContent(content);
        timelineCard.setSource(source);
        if (duration != null) {
            timelineCard.setDuration(TimelineCardType.valueOf(duration));
        }
        timelineCard.setStartAt(startAt);
        timelineCard.setEndAt(endAt);
        return timelineCard;
    }

    private NewsImage createNewsImage(Long id, News news, String url) {
        NewsImage newsImage = new NewsImage();
        newsImage.setId(id);
        newsImage.setNews(news);
        newsImage.setUrl(url);
        return newsImage;
    }

    private Tag createTag(Long id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }

    private NewsTag createNewsTag(Long id, News news, Tag tag) {
        NewsTag newsTag = new NewsTag();
        newsTag.setId(id);
        newsTag.setNews(news);
        newsTag.setTag(tag);
        return newsTag;
    }

    @Test
    void 핫이슈_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", true, user, economy);
        News news2 = createNews(2L, "제목", "미리보기 내용", true, user, entertainment);
        News news3 = createNews(3L, "제목", "미리보기 내용", true, user, sports);
        Page<News> newsPage = new PageImpl<>(List.of(news1, news2, news3));
        when(newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged()))
                .thenReturn(newsPage);

        // when
        HotissueNewsListResponse response = newsServiceImpl.getHotissueNewsCardPage();

        // then
        assertEquals(3, response.getNewsList().size());
        assertEquals(news1.getId(), response.getNewsList().get(0).getId());
        assertEquals(news2.getId(), response.getNewsList().get(1).getId());
        assertEquals(news3.getId(), response.getNewsList().get(2).getId());
    }

    @Test
    void 모든_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, economy);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, sports);
        News news4 = createNews(4L, "제목", "미리보기 내용", false, user, ktb);
        News news5 = createNews(5L, "제목", "미리보기 내용", false, user, null);

        int offset = 0;
        int page = offset / NewsServiceConstant.PAGE_SIZE;

        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3, news4, news5)));
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news2)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news4)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        MultiCategoryResponse response = newsServiceImpl.getMultiCategoryPage(user.getId(), offset);

        // then
        assertEquals(5, response.getAll().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getAll().getOffset());
        assertFalse(response.getAll().isHasNext());

        assertEquals(1, response.getEconomy().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getEconomy().getOffset());
        assertFalse(response.getEconomy().isHasNext());

        assertEquals(1, response.getEntertainment().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getEntertainment().getOffset());
        assertFalse(response.getEntertainment().isHasNext());

        assertEquals(1, response.getSports().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getSports().getOffset());
        assertFalse(response.getSports().isHasNext());

        assertEquals(1, response.getKtb().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getKtb().getOffset());
        assertFalse(response.getKtb().isHasNext());
    }

    @Test
    void 전체_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, economy);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, sports);
        News news4 = createNews(4L, "제목", "미리보기 내용", false, user, ktb);
        News news5 = createNews(5L, "제목", "미리보기 내용", false, user, null);

        int offset = 20;
        int page = offset / NewsServiceConstant.PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3, news4, news5)));
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        AllResponse response = (AllResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), null, offset);

        // then
        assertEquals(5, response.getAll().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getAll().getOffset());
        assertFalse(response.getAll().isHasNext());
    }

    @Test
    void 경제_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, economy);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, economy);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, economy);

        int offset = 20;
        int page = offset / NewsServiceConstant.PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        EconomyResponse response = (EconomyResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), economy.getName().toString(), offset);

        // then
        assertEquals(3, response.getEconomy().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getEconomy().getOffset());
        assertFalse(response.getEconomy().isHasNext());
    }

    @Test
    void 연예_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, entertainment);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, entertainment);

        int offset = 20;
        int page = offset / NewsServiceConstant.PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        EntertainmentResponse response = (EntertainmentResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), entertainment.getName().toString(), offset);

        // then
        assertEquals(3, response.getEntertainment().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getEntertainment().getOffset());
        assertFalse(response.getEntertainment().isHasNext());
    }

    @Test
    void 스포츠_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, sports);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, sports);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, sports);

        int offset = 20;
        int page = offset / NewsServiceConstant.PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        SportsResponse response = (SportsResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), sports.getName().toString(), offset);

        // then
        assertEquals(3, response.getSports().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getSports().getOffset());
        assertFalse(response.getSports().isHasNext());
    }

    @Test
    void 카테부_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        News news2 = createNews(2L, "제목", "미리보기 내용", false, user, ktb);
        News news3 = createNews(3L, "제목", "미리보기 내용", false, user, ktb);

        int offset = 20;
        int page = offset / NewsServiceConstant.PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page + 1, NewsServiceConstant.PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        KtbResponse response = (KtbResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), ktb.getName().toString(), offset);

        // then
        assertEquals(3, response.getKtb().getNewsList().size());
        assertEquals(offset + NewsServiceConstant.PAGE_SIZE, response.getKtb().getOffset());
        assertFalse(response.getKtb().isHasNext());
    }

    @Test
    void 뉴스_검색_결과_조회_검증() {
        // given
        List<String> tags = List.of("태그1", "태그2", "태그3");

        News news1 = createNews(1L, "제목1", "미리보기 내용1", false, user, sports);
        News news2 = createNews(2L, "제목2", "미리보기 내용2", false, user, sports);
        News news3 = createNews(3L, "제목3", "미리보기 내용3", false, user, sports);
        News news4 = createNews(4L, "제목4", "미리보기 내용4", false, user, sports);
        List<News> newsList = List.of(news1, news2, news3, news4);
        Page<News> newsPage = new PageImpl<>(newsList);

        when(newsRepository.searchNewsPageByTags(tags, PageRequest.of(0, NewsServiceConstant.PAGE_SIZE)))
                .thenReturn(newsPage);

        // when
        NewsListResponse response = newsServiceImpl.getSearchNewsCardPage(user.getId(), tags, 0);

        // then
        assertEquals(newsList.size(), response.getNewsList().size());
        assertEquals(NewsServiceConstant.PAGE_SIZE, response.getOffset());
        assertFalse(response.isHasNext());
    }

    @Test
    void 태그_수_미달_시_뉴스_검색_예외_처리_검증() {
        // given
        List<String> tags = List.of();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            newsServiceImpl.getSearchNewsCardPage(user.getId(), tags, 0);
        });
    }

    @Test
    void 태그_수_초과_시_뉴스_검색_예외_처리_검증() {
        // given
        List<String> tags = List.of("태그1", "태그2", "태그3", "태그4", "태그5", "태그6", "태그7");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            newsServiceImpl.getSearchNewsCardPage(user.getId(), tags, 0);
        });
    }

    @Test
    void 뉴스_검색_시_태그_중복_제거_검증() {
        // given
        List<String> tagsWithDuplicates = List.of("태그1", "태그2", "태그3", "태그3");
        List<String> tags = List.of("태그1", "태그2", "태그3");

        News news1 = createNews(1L, "제목1", "미리보기 내용1", false, user, sports);
        News news2 = createNews(2L, "제목2", "미리보기 내용2", false, user, sports);
        News news3 = createNews(3L, "제목3", "미리보기 내용3", false, user, sports);
        News news4 = createNews(4L, "제목4", "미리보기 내용4", false, user, sports);
        List<News> newsList = List.of(news1, news2, news3, news4);
        Page<News> newsPage = new PageImpl<>(newsList);

        when(newsRepository.searchNewsPageByTags(tags, PageRequest.of(0, NewsServiceConstant.PAGE_SIZE)))
                .thenReturn(newsPage);

        // when
        NewsListResponse response1 = newsServiceImpl.getSearchNewsCardPage(user.getId(), tagsWithDuplicates, 0);
        NewsListResponse response2 = newsServiceImpl.getSearchNewsCardPage(user.getId(), tags, 0);

        // then
        assertEquals(newsList.size(), response1.getNewsList().size());
        assertEquals(NewsServiceConstant.PAGE_SIZE, response1.getOffset());
        assertFalse(response1.isHasNext());

        assertEquals(newsList.size(), response2.getNewsList().size());
        assertEquals(NewsServiceConstant.PAGE_SIZE, response2.getOffset());
        assertFalse(response2.isHasNext());

        assertEquals(response1.getNewsList().get(0).getId(), response2.getNewsList().get(0).getId());
        assertEquals(response1.getNewsList().get(1).getId(), response2.getNewsList().get(1).getId());
        assertEquals(response1.getNewsList().get(2).getId(), response2.getNewsList().get(2).getId());
        assertEquals(response1.getNewsList().get(3).getId(), response2.getNewsList().get(3).getId());
    }

    @Test
    void 뉴스_상세_정보_조회_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, sports);
        NewsImage newsImage = createNewsImage(1L, news, "url");
        TimelineCard timelineCard1 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());
        TimelineCard timelineCard2 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());
        TimelineCard timelineCard3 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());

        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(newsImageRepository.findByNewsId(news.getId())).thenReturn(Optional.of(newsImage));
        when(timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId())).thenReturn(List.of(timelineCard1, timelineCard2, timelineCard3));

        // when
        NewsDetailDTO response = newsServiceImpl.getNewsDetail(news.getId(), user.getId());

        // then
        assertEquals(response.getId(), news.getId());
        assertEquals(response.getTitle(), news.getTitle());
        assertEquals(response.getImage(), newsImage.getUrl());
        assertEquals(response.getTimeline().get(0).getTitle(), timelineCard1.getTitle());
        assertEquals(response.getTimeline().get(1).getTitle(), timelineCard2.getTitle());
        assertEquals(response.getTimeline().get(2).getTitle(), timelineCard3.getTitle());
    }

    @Test
    void 뉴스_생성_검증() {
        // given
        List<String> query = List.of("키워드1", "키워드2", "키워드3");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findNewsByExactlyMatchingTags(query, query.size())).thenReturn(Optional.empty());

        // 타임라인 생성
        NewsCreateRequest newsCreateRequest = new NewsCreateRequest(query);
        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }
        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );
        LocalDate localDate = LocalDate.now();
        when(aiService.createAINews(query, localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate)).thenReturn(createAiNewsResponse);

        // 타임라인 병합
        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                dayCardDTOs.getLast().getStartAt(),
                dayCardDTOs.getFirst().getStartAt()
        );
        List<TimelineCardDTO> mergeAiNewsResponse = List.of(weekCardDTO);
        when(aiService.mergeTimelineCards(dayCardDTOs)).thenReturn(mergeAiNewsResponse);

        // 여론 통계 생성
        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                true,
                "메시지",
                new StatisticsDTO(
                        10,
                        20,
                        70
                )
        );
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAiResponse = CompletableFuture.completedFuture(statisticsDTO);
        when(asyncAiService.getAIStatistics(query)).thenReturn(statsAiResponse);

        // 뉴스 태그 저장
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("태그명");
        when(tagRepository.findByName(any(String.class))).thenReturn(Optional.of(tag));

        // when
        NewsDetailDTO response = newsServiceImpl.save(user.getId(), false, newsCreateRequest);

        // then
        verify(aiService, atLeastOnce()).createAINews(query, localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate);
        verify(aiService, atLeastOnce()).mergeTimelineCards(dayCardDTOs);
        verify(asyncAiService, times(1)).getAIStatistics(query);
        assertEquals(statisticsDTO.getData().getPositive(), response.getStatistics().getPositive());
        assertEquals(statisticsDTO.getData().getNeutral(), response.getStatistics().getNeutral());
        assertEquals(statisticsDTO.getData().getNegative(), response.getStatistics().getNegative());
    }

    @Test
    void 입력_키워드_목록과_태그_목록이_동일한_뉴스가_존재하면_뉴스_생성_대신_업데이트_검증() {
        // given
        List<String> query = List.of("키워드1", "키워드2", "키워드3");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // 타임라인 생성
        NewsCreateRequest newsCreateRequest = new NewsCreateRequest(query);

        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS));

        NewsImage newsImage = createNewsImage(1L, news, "url");

        NewsTag newsTag1 = createNewsTag(1L, news, createTag(1L, "키워드1"));
        NewsTag newsTag2 = createNewsTag(1L, news, createTag(2L, "키워드2"));
        NewsTag newsTag3 = createNewsTag(1L, news, createTag(3L, "키워드3"));
        List<NewsTag> newsTags = List.of(newsTag1, newsTag2, newsTag3);

        TimelineCard weekCard = createTimelineCard(
                news,
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(13),
                LocalDate.now().minusDays(7)
        );
        List<TimelineCard> timelineCards = List.of(weekCard);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findNewsByExactlyMatchingTags(query, query.size())).thenReturn(Optional.of(news));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId())).thenReturn(timelineCards);
        when(newsTagRepository.findByNewsId(news.getId())).thenReturn(newsTags);
        when(newsImageRepository.findByNewsId(news.getId())).thenReturn(Optional.of(newsImage));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.empty());

        // 타임라인 생성
        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }

        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );

        when(aiService.createAINews(eq(query), eq(timelineCards.getFirst().getEndAt().plusDays(1)), eq(LocalDate.now())))
                .thenReturn(createAiNewsResponse);

        // 타임라인 병합
        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                weekCard.getTitle(),
                weekCard.getContent(),
                weekCard.getSource(),
                weekCard.getDuration().toString(),
                weekCard.getStartAt(),
                weekCard.getEndAt()
        );
        TimelineCardDTO mergedTimelineCard = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(6),
                LocalDate.now()
        );
        List<TimelineCardDTO> mergedResponse = List.of(mergedTimelineCard, weekCardDTO);
        when(aiService.mergeTimelineCards(argThat(list -> list.size() == 8)))
                .thenReturn(mergedResponse);

        // 여론 통계 생성
        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                true,
                "메시지",
                new StatisticsDTO(
                        20,
                        30,
                        50
                )
        );
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAiResponse = CompletableFuture.completedFuture(statisticsDTO);
        when(asyncAiService.getAIStatistics(query)).thenReturn(statsAiResponse);

        // when
        NewsDetailDTO response = newsServiceImpl.save(user.getId(), false, newsCreateRequest);

        // then
        assertEquals(createAiNewsResponse.getData().getTitle(), response.getTitle());
        assertEquals(mergedResponse.size(), response.getTimeline().size());
        assertEquals(statisticsDTO.getData().getPositive(), response.getStatistics().getPositive());
        assertEquals(statisticsDTO.getData().getNeutral(), response.getStatistics().getNeutral());
        assertEquals(statisticsDTO.getData().getNegative(), response.getStatistics().getNegative());
    }

    @Test
    void 여론_통계_생성_API가_404_반환_시_뉴스_생성_성공_검증() {
        // given
        List<String> query = List.of("키워드1", "키워드2", "키워드3");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findNewsByExactlyMatchingTags(query, query.size())).thenReturn(Optional.empty());

        // 타임라인 생성
        NewsCreateRequest newsCreateRequest = new NewsCreateRequest(query);
        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }
        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );
        LocalDate localDate = LocalDate.now();
        when(aiService.createAINews(query, localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate)).thenReturn(createAiNewsResponse);

        // 타임라인 병합
        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                dayCardDTOs.getLast().getStartAt(),
                dayCardDTOs.getFirst().getStartAt()
        );
        List<TimelineCardDTO> mergeAiNewsResponse = List.of(weekCardDTO);
        when(aiService.mergeTimelineCards(dayCardDTOs)).thenReturn(mergeAiNewsResponse);

        // 여론 통계 생성
        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                false,
                "메시지",
                null
        );
        when(asyncAiService.getAIStatistics(query)).thenReturn(
                CompletableFuture.failedFuture(new AIException(HttpStatus.NOT_FOUND, statisticsDTO))
        );

        // 뉴스 태그 저장
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("태그명");
        when(tagRepository.findByName(any(String.class))).thenReturn(Optional.of(tag));

        // when
        NewsDetailDTO response = newsServiceImpl.save(user.getId(), false, newsCreateRequest);

        // then
        verify(aiService, atLeastOnce()).createAINews(anyList(), any(LocalDate.class), any(LocalDate.class));
        verify(aiService, atLeastOnce()).mergeTimelineCards(anyList());
        verify(asyncAiService, times(1)).getAIStatistics(anyList());
        assertEquals(0, response.getStatistics().getPositive());
        assertEquals(0, response.getStatistics().getNeutral());
        assertEquals(0, response.getStatistics().getNegative());
    }

    @Test
    void KTB_뉴스_생성_검증() {
        // given
        TimelineCardDTO timelineCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("출처1", "출처2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now(),
                LocalDate.now()
        );
        KtbNewsCreateRequest ktbNewsCreateRequest = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                "이미지 url",
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        // when
        NewsDetailDTO response = newsServiceImpl.saveKtbNews(admin.getId(), ktbNewsCreateRequest);

        // then
        verify(newsRepository, times(1)).save(any(News.class));
        verify(newsImageRepository, times(1)).save(any(NewsImage.class));
        verify(timelineCardRepository, atLeastOnce()).save(any(TimelineCard.class));
    }

    @Test
    void KTB_뉴스_이미지_없이_생성_검증() {
        // given
        TimelineCardDTO timelineCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("출처1", "출처2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now(),
                LocalDate.now()
        );
        KtbNewsCreateRequest ktbNewsCreateRequest = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                null,
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        // when
        NewsDetailDTO response = newsServiceImpl.saveKtbNews(admin.getId(), ktbNewsCreateRequest);

        // then
        verify(newsRepository, times(1)).save(any(News.class));
        verify(newsImageRepository, times(0)).save(any(NewsImage.class));
        verify(timelineCardRepository, atLeastOnce()).save(any(TimelineCard.class));
    }

    @Test
    void KTB_뉴스_타임라인_카드_출처_없이_생성_검증() {
        // given
        TimelineCardDTO timelineCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                null,
                TimelineCardType.DAY.toString(),
                LocalDate.now(),
                LocalDate.now()
        );
        KtbNewsCreateRequest ktbNewsCreateRequest = new KtbNewsCreateRequest(
                "제목",
                "미리보기 내용",
                "이미지 url",
                List.of(timelineCardDTO, timelineCardDTO, timelineCardDTO)
        );

        when(userRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        // when
        NewsDetailDTO response = newsServiceImpl.saveKtbNews(admin.getId(), ktbNewsCreateRequest);

        // then
        verify(newsRepository, times(1)).save(any(News.class));
        verify(newsImageRepository, times(1)).save(any(NewsImage.class));
        verify(timelineCardRepository, atLeastOnce()).save(any(TimelineCard.class));
    }

    @Test
    void 뉴스_업데이트_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS));

        NewsImage newsImage = createNewsImage(1L, news, "url");

        NewsTag newsTag1 = createNewsTag(1L, news, createTag(1L, "태그1"));
        NewsTag newsTag2 = createNewsTag(1L, news, createTag(2L, "태그2"));
        List<NewsTag> newsTags = List.of(newsTag1, newsTag2);

        List<String> query = List.of(newsTag1.getTag().getName(), newsTag2.getTag().getName());

        TimelineCard weekCard = createTimelineCard(
                news,
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(13),
                LocalDate.now().minusDays(7)
        );
        List<TimelineCard> timelineCards = List.of(weekCard);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId())).thenReturn(timelineCards);
        when(newsTagRepository.findByNewsId(news.getId())).thenReturn(newsTags);
        when(newsImageRepository.findByNewsId(news.getId())).thenReturn(Optional.of(newsImage));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.empty());

        // 타임라인 생성
        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }

        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );

        when(aiService.createAINews(eq(query), eq(timelineCards.getFirst().getEndAt().plusDays(1)), eq(LocalDate.now())))
                .thenReturn(createAiNewsResponse);

        // 타임라인 병합
        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                weekCard.getTitle(),
                weekCard.getContent(),
                weekCard.getSource(),
                weekCard.getDuration().toString(),
                weekCard.getStartAt(),
                weekCard.getEndAt()
        );
        TimelineCardDTO mergedTimelineCard = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(6),
                LocalDate.now()
        );
        List<TimelineCardDTO> mergedResponse = List.of(mergedTimelineCard, weekCardDTO);
        when(aiService.mergeTimelineCards(argThat(list -> list.size() == 8)))
                .thenReturn(mergedResponse);

        // 여론 통계 생성
        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                true,
                "메시지",
                new StatisticsDTO(
                        20,
                        30,
                        50
                )
        );
        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAiResponse = CompletableFuture.completedFuture(statisticsDTO);
        when(asyncAiService.getAIStatistics(query)).thenReturn(statsAiResponse);

        // when
        NewsDetailDTO response = newsServiceImpl.update(news.getId(), user.getId(), false);

        // then
        assertEquals(createAiNewsResponse.getData().getTitle(), response.getTitle());
        assertEquals(mergedResponse.size(), response.getTimeline().size());
        assertEquals(statisticsDTO.getData().getPositive(), response.getStatistics().getPositive());
        assertEquals(statisticsDTO.getData().getNeutral(), response.getStatistics().getNeutral());
        assertEquals(statisticsDTO.getData().getNegative(), response.getStatistics().getNegative());
    }

    @Test
    void 여론_통계_생성_API가_404_반환_시_뉴스_업데이트_성공_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS));

        NewsImage newsImage = createNewsImage(1L, news, "url");

        NewsTag newsTag1 = createNewsTag(1L, news, createTag(1L, "태그1"));
        NewsTag newsTag2 = createNewsTag(1L, news, createTag(2L, "태그2"));
        List<NewsTag> newsTags = List.of(newsTag1, newsTag2);

        List<String> query = List.of(newsTag1.getTag().getName(), newsTag2.getTag().getName());

        TimelineCard weekCard = createTimelineCard(
                news,
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(13),
                LocalDate.now().minusDays(7)
        );
        List<TimelineCard> timelineCards = List.of(weekCard);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId())).thenReturn(timelineCards);
        when(newsTagRepository.findByNewsId(news.getId())).thenReturn(newsTags);
        when(newsImageRepository.findByNewsId(news.getId())).thenReturn(Optional.of(newsImage));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.empty());

        // 타임라인 생성
        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }

        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );
        when(aiService.createAINews(eq(query), eq(timelineCards.getFirst().getEndAt().plusDays(1)), eq(LocalDate.now())))
                .thenReturn(createAiNewsResponse);

        // 타임라인 병합
        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                weekCard.getTitle(),
                weekCard.getContent(),
                weekCard.getSource(),
                weekCard.getDuration().toString(),
                weekCard.getStartAt(),
                weekCard.getEndAt()
        );
        TimelineCardDTO mergedTimelineCard = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(6),
                LocalDate.now()
        );
        List<TimelineCardDTO> mergedResponse = List.of(mergedTimelineCard, weekCardDTO);
        when(aiService.mergeTimelineCards(argThat(list -> list.size() == 8)))
                .thenReturn(mergedResponse);

        // 여론 통계 생성
        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                false,
                "메시지",
                null
        );
        when(asyncAiService.getAIStatistics(query)).thenReturn(
                CompletableFuture.failedFuture(new AIException(HttpStatus.NOT_FOUND, statisticsDTO))
        );

        // when
        NewsDetailDTO response = newsServiceImpl.update(news.getId(), user.getId(), false);

        // then
        verify(aiService, atLeastOnce()).createAINews(anyList(), any(LocalDate.class), any(LocalDate.class));
        verify(aiService, atLeastOnce()).mergeTimelineCards(anyList());
        verify(asyncAiService, times(1)).getAIStatistics(anyList());
        assertEquals(0, response.getStatistics().getPositive());
        assertEquals(0, response.getStatistics().getNeutral());
        assertEquals(0, response.getStatistics().getNegative());
    }

    @Test
    void 뉴스_업데이트_시간_제약_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            newsServiceImpl.update(news.getId(), user.getId(), false);
        });

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(NewsResponseMessage.NEWS_DELETE_CONFLICT, exception.getReason());
    }

    @Test
    void 뉴스_삭제_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now().minusDays(NewsServiceConstant.NEWS_DELETE_DAYS));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(Role.ADMIN);
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        // when
        newsServiceImpl.delete(news.getId(), user.getId());

        // then
        verify(newsRepository).delete(news);
    }

    @Test
    void 뉴스_삭제_권한_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now().minusDays(NewsServiceConstant.NEWS_DELETE_DAYS));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(Role.USER);

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            newsServiceImpl.delete(news.getId(), user.getId());
        });

        // then
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(NewsResponseMessage.NEWS_DELETE_FORBIDDEN, exception.getReason());
    }

    @Test
    void 뉴스_삭제_시간_제약_검증() {
        // given
        News news = createNews(1L, "제목", "미리보기 내용", false, user, ktb);
        news.setUpdatedAt(LocalDateTime.now());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(user.getRole()).thenReturn(Role.ADMIN);
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            newsServiceImpl.delete(news.getId(), user.getId());
        });

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(NewsResponseMessage.NEWS_UPDATE_CONFLICT, exception.getReason());
    }

    @Test
    void 핫이슈_뉴스_생성_검증() {
        // given
        List<String> keywords = List.of("키워드1", "키워드2", "키워드3");
        AIHotissueResponse aiHotissueResponse = new AIHotissueResponse(keywords);
        WrappedDTO<AIHotissueResponse> WrappedResponse = new WrappedDTO<>(true, "메시지", aiHotissueResponse);

        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }

        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );

        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                dayCardDTOs.getLast().getStartAt(),
                dayCardDTOs.getFirst().getStartAt()
        );

        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                true,
                "메시지",
                new StatisticsDTO(
                        10,
                        20,
                        70
                )
        );

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("태그명");

        when(aiService.createAIHotissueKeywords()).thenReturn(WrappedResponse);
        when(newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged())).thenReturn(Page.empty());
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(0)), 1)).thenReturn(Optional.empty());
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(1)), 1)).thenReturn(Optional.empty());
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(2)), 1)).thenReturn(Optional.empty());

        LocalDate localDate = LocalDate.now();
        when(aiService.createAINews(List.of(keywords.get(0)), localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate)).thenReturn(createAiNewsResponse);
        when(aiService.createAINews(List.of(keywords.get(1)), localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate)).thenReturn(createAiNewsResponse);
        when(aiService.createAINews(List.of(keywords.get(2)), localDate.minusDays(NewsServiceConstant.NEWS_CREATE_DAYS), localDate)).thenReturn(createAiNewsResponse);
        when(aiService.mergeTimelineCards(dayCardDTOs)).thenReturn(List.of(weekCardDTO));

        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAiResponse = CompletableFuture.completedFuture(statisticsDTO);
        when(asyncAiService.getAIStatistics(List.of(keywords.get(0)))).thenReturn(statsAiResponse);
        when(asyncAiService.getAIStatistics(List.of(keywords.get(1)))).thenReturn(statsAiResponse);
        when(asyncAiService.getAIStatistics(List.of(keywords.get(2)))).thenReturn(statsAiResponse);
        when(tagRepository.findByName(any(String.class))).thenReturn(Optional.of(tag));

        // when
        newsServiceImpl.createHotissueNews();

        // then
        verify(newsRepository, times(3)).save(any(News.class));
        verify(timelineCardRepository, atLeastOnce()).save(any(TimelineCard.class));
        verify(newsImageRepository, times(3)).save(any(NewsImage.class));
        verify(newsTagRepository, times(3)).save(any(NewsTag.class));
        verify(asyncAiService, times(3)).getAIStatistics(anyList());
        verify(aiService, times(3)).createAINews(anyList(), any(LocalDate.class), any(LocalDate.class));
        verify(aiService, times(3)).mergeTimelineCards(anyList());
    }

    @Test
    void 핫이슈_키워드와_태그_목록이_동일한_뉴스는_핫이슈_전환_검증() {
        // given
        News news1 = createNews(1L, "제목1", "미리보기 내용2", false, user, ktb);
        news1.setUpdatedAt(LocalDateTime.now().minusHours(NewsServiceConstant.NEWS_UPDATE_HOURS));

        News news2 = createNews(2L, "제목2", "미리보기 내용2", false, user, economy);
        news2.setUpdatedAt(LocalDateTime.now());

        NewsImage newsImage = createNewsImage(1L, news1, "url1");
        NewsTag newsTag = createNewsTag(1L, news1, createTag(1L, "키워드1"));

        TimelineCard weekCard = createTimelineCard(
                news1,
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                LocalDate.now().minusDays(13),
                LocalDate.now().minusDays(7)
        );
        List<TimelineCard> timelineCards = List.of(weekCard);

        List<TimelineCardDTO> dayCardDTOs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate localDate = LocalDate.now().minusDays(i);

            TimelineCardDTO dayCardDTO = new TimelineCardDTO(
                    "제목",
                    "내용",
                    List.of("source1", "source2"),
                    TimelineCardType.DAY.toString(),
                    localDate,
                    localDate
            );

            dayCardDTOs.add(dayCardDTO);
        }

        WrappedDTO<AINewsResponse> createAiNewsResponse = new WrappedDTO<>(
                true,
                "메시지",
                new AINewsResponse(
                        "제목",
                        "미리보기 내용",
                        "이미지",
                        CategoryType.SPORTS.toString(),
                        dayCardDTOs
                )
        );

        TimelineCardDTO weekCardDTO = new TimelineCardDTO(
                "제목",
                "내용",
                List.of("source1", "source2"),
                TimelineCardType.WEEK.toString(),
                dayCardDTOs.getLast().getStartAt(),
                dayCardDTOs.getFirst().getStartAt()
        );

        WrappedDTO<StatisticsDTO> statisticsDTO = new WrappedDTO<>(
                true,
                "메시지",
                new StatisticsDTO(
                        10,
                        20,
                        70
                )
        );

        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("태그명");

        List<String> keywords = List.of("키워드1", "키워드2", "키워드3");
        AIHotissueResponse aiHotissueResponse = new AIHotissueResponse(keywords);
        WrappedDTO<AIHotissueResponse> WrappedResponse = new WrappedDTO<>(true, "메시지", aiHotissueResponse);

        when(aiService.createAIHotissueKeywords()).thenReturn(WrappedResponse);
        when(newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged())).thenReturn(Page.empty());
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(0)), 1)).thenReturn(Optional.empty());
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(1)), 1)).thenReturn(Optional.of(news1));
        when(newsRepository.findNewsByExactlyMatchingTags(List.of(keywords.get(2)), 1)).thenReturn(Optional.of(news2));

        when(newsRepository.findById(news1.getId())).thenReturn(Optional.of(news1));
        when(newsRepository.findById(news2.getId())).thenReturn(Optional.of(news2));

        when(timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news1.getId())).thenReturn(timelineCards);
        when(newsTagRepository.findByNewsId(news1.getId())).thenReturn(List.of(newsTag));
        when(newsImageRepository.findByNewsId(news1.getId())).thenReturn(Optional.of(newsImage));

        when(aiService.createAINews(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(createAiNewsResponse);
        when(aiService.mergeTimelineCards(dayCardDTOs)).thenReturn(List.of(weekCardDTO));

        CompletableFuture<WrappedDTO<StatisticsDTO>> statsAiResponse = CompletableFuture.completedFuture(statisticsDTO);
        when(asyncAiService.getAIStatistics(anyList())).thenReturn(statsAiResponse);
        when(tagRepository.findByName(any(String.class))).thenReturn(Optional.of(tag));

        // when
        newsServiceImpl.createHotissueNews();

        // then
        verify(userRepository, times(0)).findById(any(Long.class));
        verify(newsRepository, times(3)).save(any(News.class));
        verify(newsRepository, times(2)).findById(any(Long.class));
        verify(timelineCardRepository, atLeastOnce()).save(any(TimelineCard.class));
        verify(aiService, times(2)).createAINews(anyList(), any(LocalDate.class), any(LocalDate.class));
        verify(aiService, times(2)).mergeTimelineCards(anyList());
        verify(asyncAiService, times(2)).getAIStatistics(anyList());
        verify(newsImageRepository, times(2)).save(any(NewsImage.class));
        verify(newsTagRepository, times(1)).save(any(NewsTag.class));

        assertTrue(news1.getIsHotissue());
        assertTrue(news2.getIsHotissue());
    }

    @Test
    void 핫이슈_생성_시_기존_핫이슈_뉴스들은_일반_뉴스로_전환_검증() {
        // given
        AIHotissueResponse aiHotissueResponse = new AIHotissueResponse(List.of());
        WrappedDTO<AIHotissueResponse> WrappedResponse = new WrappedDTO<>(true, "메시지", aiHotissueResponse);

        News news1 = createNews(1L, "제목1", "미리보기 내용2", true, user, ktb);
        News news2 = createNews(2L, "제목2", "미리보기 내용2", true, user, economy);
        News news3 = createNews(3L, "제목3", "미리보기 내용3", true, user, sports);
        Page<News> previousNewsPage = new PageImpl<>(Arrays.asList(news1, news2, news3));

        when(aiService.createAIHotissueKeywords()).thenReturn(WrappedResponse);
        when(newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged())).thenReturn(previousNewsPage);

        // when
        newsServiceImpl.createHotissueNews();

        // then
        verify(newsRepository).updateIsHotissue(news1.getId(), false);
        verify(newsRepository).updateIsHotissue(news2.getId(), false);
        verify(newsRepository).updateIsHotissue(news3.getId(), false);
    }

    @Test
    void 오래된_뉴스_및_고아_태그_삭제_검증() {
        // when
        newsServiceImpl.deleteOldNewsAndOrphanTags();

        // then
        verify(newsRepository, times(1)).deleteAllOlderThan(any(LocalDateTime.class));
        verify(tagRepository, times(1)).deleteAllOrphan();
    }
}
