package com.tamnara.backend.news.service;

import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
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
import com.tamnara.backend.user.domain.State;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

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

    private static final int PAGE_SIZE = 20;

    User user;
    Category economy;
    Category entertainment;
    Category sports;
    Category ktb;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("이메일");
        lenient().when(user.getPassword()).thenReturn("비밀번호");
        lenient().when(user.getUsername()).thenReturn("이름");
        lenient().when(user.getProvider()).thenReturn("LOCAL");
        lenient().when(user.getProviderId()).thenReturn(null);
        lenient().when(user.getRole()).thenReturn(Role.USER);
        lenient().when(user.getState()).thenReturn(State.ACTIVE);

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

    private News createNews(String title, String summary, Boolean isHotissue, User user, Category category) {
        News news = new News();
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

    private NewsImage createNewsImage(News news, String url) {
        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl(url);
        return newsImage;
    }

    @Test
    void 핫이슈_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", true, user, economy);
        News news2 = createNews("제목", "미리보기 내용", true, user, entertainment);
        News news3 = createNews("제목", "미리보기 내용", true, user, sports);
        Page<News> newsPage = new PageImpl<>(List.of(news1, news2, news3));
        when(newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged()))
                .thenReturn(newsPage);

        // when
        HotissueNewsListResponse response = newsServiceImpl.getHotissueNewsCardPage();

        // then
        assertEquals(3, response.getNewsList().size());
        assertEquals(news1.getId(), response.getNewsList().get(2).getId());
        assertEquals(news2.getId(), response.getNewsList().get(1).getId());
        assertEquals(news3.getId(), response.getNewsList().get(0).getId());
    }

    @Test
    void 모든_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, economy);
        News news2 = createNews("제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews("제목", "미리보기 내용", false, user, sports);
        News news4 = createNews("제목", "미리보기 내용", false, user, ktb);
        News news5 = createNews("제목", "미리보기 내용", false, user, null);

        int offset = 0;
        int page = offset / PAGE_SIZE;

        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3, news4, news5)));
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news2)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news4)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        MultiCategoryResponse response = newsServiceImpl.getMultiCategoryPage(user.getId(), offset);

        // then
        assertEquals(5, response.getAll().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getAll().getOffset());
        assertFalse(response.getAll().isHasNext());

        assertEquals(1, response.getEconomy().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getEconomy().getOffset());
        assertFalse(response.getEconomy().isHasNext());

        assertEquals(1, response.getEntertainment().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getEntertainment().getOffset());
        assertFalse(response.getEntertainment().isHasNext());

        assertEquals(1, response.getSports().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getSports().getOffset());
        assertFalse(response.getSports().isHasNext());

        assertEquals(1, response.getKtb().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getKtb().getOffset());
        assertFalse(response.getKtb().isHasNext());
    }

    @Test
    void 전체_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, economy);
        News news2 = createNews("제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews("제목", "미리보기 내용", false, user, sports);
        News news4 = createNews("제목", "미리보기 내용", false, user, ktb);
        News news5 = createNews("제목", "미리보기 내용", false, user, null);

        int offset = 20;
        int page = offset / PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3, news4, news5)));
        when(newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        AllResponse response = (AllResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), null, offset);

        // then
        assertEquals(5, response.getAll().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getAll().getOffset());
        assertFalse(response.getAll().isHasNext());
    }

    @Test
    void 경제_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, economy);
        News news2 = createNews("제목", "미리보기 내용", false, user, economy);
        News news3 = createNews("제목", "미리보기 내용", false, user, economy);

        int offset = 20;
        int page = offset / PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(economy.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        EconomyResponse response = (EconomyResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), economy.getName().toString(), offset);

        // then
        assertEquals(3, response.getEconomy().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getEconomy().getOffset());
        assertFalse(response.getEconomy().isHasNext());
    }

    @Test
    void 연예_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, entertainment);
        News news2 = createNews("제목", "미리보기 내용", false, user, entertainment);
        News news3 = createNews("제목", "미리보기 내용", false, user, entertainment);

        int offset = 20;
        int page = offset / PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(entertainment.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        EntertainmentResponse response = (EntertainmentResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), entertainment.getName().toString(), offset);

        // then
        assertEquals(3, response.getEntertainment().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getEntertainment().getOffset());
        assertFalse(response.getEntertainment().isHasNext());
    }

    @Test
    void 스포츠_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, sports);
        News news2 = createNews("제목", "미리보기 내용", false, user, sports);
        News news3 = createNews("제목", "미리보기 내용", false, user, sports);

        int offset = 20;
        int page = offset / PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(sports.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        SportsResponse response = (SportsResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), sports.getName().toString(), offset);

        // then
        assertEquals(3, response.getSports().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getSports().getOffset());
        assertFalse(response.getSports().isHasNext());
    }

    @Test
    void 카테부_카테고리_뉴스_카드_목록_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", false, user, ktb);
        News news2 = createNews("제목", "미리보기 내용", false, user, ktb);
        News news3 = createNews("제목", "미리보기 내용", false, user, ktb);

        int offset = 20;
        int page = offset / PAGE_SIZE;
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of(news1, news2, news3)));
        when(newsRepository.findByIsHotissueFalseAndCategoryId(eq(ktb.getId()), eq(PageRequest.of(page + 1, PAGE_SIZE)))).thenReturn(new PageImpl<>(List.of()));

        // when
        KtbResponse response = (KtbResponse) newsServiceImpl.getSingleCategoryPage(user.getId(), ktb.getName().toString(), offset);

        // then
        assertEquals(3, response.getKtb().getNewsList().size());
        assertEquals(offset + PAGE_SIZE, response.getKtb().getOffset());
        assertFalse(response.getKtb().isHasNext());
    }

    @Test
    void 뉴스_상세_정보_조회_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", false, user, sports);
        NewsImage newsImage = createNewsImage(news, "url");
        TimelineCard timelineCard1 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());
        TimelineCard timelineCard2 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());
        TimelineCard timelineCard3 = createTimelineCard(news, "제목", "내용", List.of("source1", "source2"), null, LocalDate.now(), LocalDate.now());

        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(newsImageRepository.findByNewsId(newsImage.getId())).thenReturn(Optional.of(newsImage));
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
//
//    @Test
//    void 뉴스_생성_검증() {
//        // given
//
//        // when
//
//        // then
//    }
//
//    @Test
//    void 뉴스_업데이트_검증() {
//        // given
//
//        // when
//
//        // then
//    }
//
//    @Test
//    void 뉴스_삭제_검증() {
//        // given
//
//        // when
//
//        // then
//    }
}
