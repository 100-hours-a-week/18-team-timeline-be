package com.tamnara.backend.news.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.news.constant.NewsServiceConstant;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.domain.NewsTag;
import com.tamnara.backend.news.domain.Tag;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private NewsRepository newsRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private NewsTagRepository newsTagRepository;
    @Autowired private NewsImageRepository newsImageRepository;
    @Autowired private TimelineCardRepository timelineCardRepository;

    private News createNews(String title, String summary, User user, Category category) {
        News news = new News();
        news.setTitle(title);
        news.setSummary(summary);
        news.setUser(user);
        news.setCategory(category);
        return news;
    }

    private NewsTag createNewsTag(News news, Tag tag) {
        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag);
        return newsTag;
    }

    User user;
    Category category;
    Tag tag1;
    Tag tag2;
    Tag tag3;

    @BeforeEach
    void setUp() {
        newsTagRepository.deleteAll();
        tagRepository.deleteAll();
        newsRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        em.flush();
        em.clear();

        User user = User.builder()
                .email("이메일")
                .password("비밀번호")
                .username("이름")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.save(user);

        category = new Category();
        category.setName(CategoryType.KTB);
        category.setNum(4L);
        categoryRepository.save(category);

        tag1 = new Tag();
        tag1.setName("태그1");
        tagRepository.saveAndFlush(tag1);

        tag2 = new Tag();
        tag2.setName("태그2");
        tagRepository.saveAndFlush(tag2);

        tag3 = new Tag();
        tag3.setName("태그3");
        tagRepository.saveAndFlush(tag3);

        em.clear();
    }

    @Test
    void 뉴스_생성_성공_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);

        // when
        Optional<News> findNews = newsRepository.findById(news.getId());

        // then
        assertEquals(news.getId(), findNews.get().getId());
    }

    @Test
    void 뉴스_제목_null_불가_검증() {
        // given
        News news = new News();

        // when
        news.setSummary("미리보기 내용");
        news.setCategory(category);

        // then
        assertNull(news.getTitle());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsRepository.saveAndFlush(news);
        });
    }

    @Test
    void 뉴스_제목_길이_제약_검증() {
        // given
        String title = "가".repeat(256);

        // when
        News news = createNews(title, "미리보기 내용", user, category);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
           newsRepository.saveAndFlush(news);
        });
    }

    @Test
    void 뉴스_미리보기_내용_null_불가_검증() {
        // given
        News news = new News();

        // when
        news.setTitle("제목");
        news.setCategory(category);

        // then
        assertNull(news.getSummary());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsRepository.saveAndFlush(news);
        });
    }

    @Test
    void 뉴스_미리보기_내용_길이_제약_검증() {
        // given
        String summary = "가".repeat(256);

        // when
        News news = createNews("제목", summary, user, category);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsRepository.saveAndFlush(news);
        });
    }

    @Test
    void 뉴스_회원_없이_저장_가능_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", null, category);

        // when & then
        News saved = newsRepository.saveAndFlush(news);
        assertNotNull(saved.getId());
        assertNull(saved.getUser());
    }

    @Test
    void 뉴스_카테고리_없이_저장_가능_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, null);

        // when & then
        News saved = newsRepository.saveAndFlush(news);
        assertNotNull(saved.getId());
        assertNull(saved.getCategory());
    }

    @Test
    void 뉴스_생성일자와_수정일자_자동_생성_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);

        // when
        Optional<News> findNews = newsRepository.findById(news.getId());

        // then
        assertNotNull(findNews.get().getCreatedAt());
        assertNotNull(findNews.get().getUpdatedAt());
    }

    @Test
    void 뉴스_업데이트_성공_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);

        // when
        News findNews = newsRepository.findById(news.getId()).get();
        findNews.setSummary("새로운 미리보기 내용");
        findNews.setViewCount(news.getViewCount() + 1);
        findNews.setUpdateCount(news.getUpdateCount() + 1);
        findNews.setRatioPosi(20);
        findNews.setRatioNeut(40);
        findNews.setRatioNega(40);
        newsRepository.saveAndFlush(findNews);

        // then
        News updatedNews = newsRepository.findById(news.getId()).get();
        assertEquals(findNews.getTitle(), updatedNews.getTitle());
        assertEquals(findNews.getSummary(), updatedNews.getSummary());
        assertEquals(findNews.getIsHotissue(), updatedNews.getIsHotissue());
        assertEquals(findNews.getViewCount(), updatedNews.getViewCount());
        assertEquals(findNews.getUpdateCount(), updatedNews.getUpdateCount());
        assertEquals(findNews.getRatioPosi(), updatedNews.getRatioPosi());
        assertEquals(findNews.getRatioNeut(), updatedNews.getRatioNeut());
        assertEquals(findNews.getRatioNega(), updatedNews.getRatioNega());
    }

    @Test
    void 뉴스_핫이슈_전환_시_수정일자_변경되지_않고_유지_검증() {
        // given
        News news1 = createNews("제목1", "미리보기 내용1", user, category);
        news1.setIsHotissue(true);
        newsRepository.save(news1);
        LocalDateTime news1UpdatedAt = news1.getUpdatedAt();

        News news2 = createNews("제목2", "미리보기 내용2", user, category);
        news2.setIsHotissue(false);
        newsRepository.save(news2);
        LocalDateTime news2UpdatedAt = news2.getUpdatedAt();

        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        // when
        newsRepository.updateIsHotissue(news1.getId(), false);
        newsRepository.updateIsHotissue(news2.getId(), true);
        em.clear();

        // then
        News updatedNews1 = newsRepository.findById(news1.getId()).get();
        assertFalse(updatedNews1.getIsHotissue());
        assertEquals(news1UpdatedAt.truncatedTo(ChronoUnit.SECONDS), updatedNews1.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));

        News updatedNews2 = newsRepository.findById(news2.getId()).get();
        assertTrue(updatedNews2.getIsHotissue());
        assertEquals(news2UpdatedAt.truncatedTo(ChronoUnit.SECONDS), updatedNews2.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void 뉴스_조회수_증가_시_수정일자_변경되지_않고_유지_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);
        Long viewCount = news.getViewCount();
        LocalDateTime updateTime = news.getUpdatedAt();

        // when
        newsRepository.increaseViewCount(news.getId());
        em.flush();
        em.clear();

        // then
        News updatedNews = newsRepository.findById(news.getId()).get();
        assertEquals(viewCount + 1, updatedNews.getViewCount());
        assertEquals(updateTime.truncatedTo(ChronoUnit.SECONDS), updatedNews.getUpdatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    void 뉴스_회원_필드_업데이트_불가_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);

        // when
        News findNews = newsRepository.findById(news.getId()).get();
        findNews.setUser(null);

        // then
        assertEquals(news.getUser(), findNews.getUser());
    }

    @Test
    void 뉴스_삭제_성공_검증() {
        // given
        News news = createNews("제목","미리보기 요약", user, category);
        newsRepository.saveAndFlush(news);

        // when
        News findNews = newsRepository.findById(news.getId()).get();
        newsRepository.delete(findNews);

        // then
        assertFalse(newsRepository.findById(news.getId()).isPresent());
    }

    @Test
    void 수정시간이_기준시간보다_오래된_뉴스_일괄_조회_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", user, category);
        news1.setIsHotissue(true);
        newsRepository.saveAndFlush(news1);

        News news2 = createNews("제목", "미리보기 내용", user, category);
        news2.setIsHotissue(true);
        newsRepository.saveAndFlush(news2);

        LocalDateTime cutoff;
        try {
            Thread.sleep(1000); // 1초
            cutoff = LocalDateTime.now();
            Thread.sleep(1000); // 1초
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        News news3 = createNews("제목", "미리보기 내용", user, category);
        news3.setIsHotissue(true);
        newsRepository.saveAndFlush(news3);

        // when
        List<News> newsList = newsRepository.findAllOlderThan(cutoff);

        // then
        assertEquals(2, newsList.size());
        assertEquals(news1.getId(), newsList.get(0).getId());
        assertEquals(news2.getId(), newsList.get(1).getId());
    }

    @Test
    void 수정시간이_기준시간보다_오래된_뉴스_일괄_삭제_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", user, category);
        news1.setIsHotissue(true);
        newsRepository.saveAndFlush(news1);

        News news2 = createNews("제목", "미리보기 내용", user, category);
        news2.setIsHotissue(true);
        newsRepository.saveAndFlush(news2);

        LocalDateTime cutoff;
        try {
            Thread.sleep(1000); // 1초
            cutoff = LocalDateTime.now();
            Thread.sleep(1000); // 1초
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        News news3 = createNews("제목", "미리보기 내용", user, category);
        news3.setIsHotissue(true);
        newsRepository.saveAndFlush(news3);

        // when
        newsRepository.deleteAllOlderThan(cutoff);
        em.flush();
        em.clear();

        // then
        assertFalse(newsRepository.findById(news1.getId()).isPresent());
        assertFalse(newsRepository.findById(news2.getId()).isPresent());
        assertTrue(newsRepository.findById(news3.getId()).isPresent());
    }

    @Test
    void 핫이슈_뉴스_목록_조회_시_ID_오름차순_정렬_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", user, category);
        news1.setIsHotissue(true);
        newsRepository.saveAndFlush(news1);

        News news2 = createNews("제목", "미리보기 내용", user, category);
        news2.setIsHotissue(true);
        newsRepository.saveAndFlush(news2);

        News news3 = createNews("제목", "미리보기 내용", user, category);
        news3.setIsHotissue(true);
        newsRepository.saveAndFlush(news3);

        // when
        Page<News> hotissuePage = newsRepository.findAllByIsHotissueTrueOrderByIdAsc(Pageable.unpaged());
        List<News> hotissueList = hotissuePage.getContent();

        // then
        assertEquals(3, hotissueList.size());
        assertTrue(hotissueList.stream().allMatch(News::getIsHotissue));

        assertTrue(hotissueList.get(0).getId() < hotissueList.get(1).getId());
        assertTrue(hotissueList.get(1).getId() < hotissueList.get(2).getId());
    }

    @Test
    void 일반_뉴스_목록_전체_조회_시_정렬_및_페이징_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news1);
        News news2 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news2);
        News news3 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news3);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<News> normalPage = newsRepository.findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(pageable);
        List<News> normalList = normalPage.getContent();

        // then
        assertEquals(2, normalList.size());
        assertFalse(normalList.stream().allMatch(News::getIsHotissue));

        assertEquals(normalList.get(0).getId(), news3.getId());
        assertEquals(normalList.get(1).getId(), news2.getId());

        News first = normalList.get(0);
        News second = normalList.get(1);
        assertTrue(
                first.getUpdatedAt().isAfter(second.getUpdatedAt()) ||
                        (first.getUpdatedAt().isEqual(second.getUpdatedAt()) && first.getId() > second.getId())
        );
    }

    @Test
    void 특정_카테고리의_일반_뉴스_목록_조회_시_페이징_및_정렬_검증() {
        // given
        News news1 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news1);
        News news2 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news2);
        News news3 = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news3);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<News> normalCategoryPage = newsRepository.findByIsHotissueFalseAndCategoryId(category.getId(), pageable);
        List<News> normalCategoryList = normalCategoryPage.getContent();

        // then
        assertEquals(2, normalCategoryList.size());
        assertFalse(normalCategoryList.stream().allMatch(News::getIsHotissue));
        assertTrue(normalCategoryList.stream().allMatch(news -> news.getCategory().equals(category)));

        assertEquals(normalCategoryList.get(0).getId(), news3.getId());
        assertEquals(normalCategoryList.get(1).getId(), news2.getId());

        News first = normalCategoryList.get(0);
        News second = normalCategoryList.get(1);
        assertTrue(
                first.getUpdatedAt().isAfter(second.getUpdatedAt()) ||
                        (first.getUpdatedAt().isEqual(second.getUpdatedAt()) && first.getId() > second.getId())
        );
    }

    @Test
    void 뉴스_삭제_시_연관관계_CASCADE_삭제_검증() {
        // given
        Tag tag = new Tag();
        tag.setName("태그");
        tagRepository.saveAndFlush(tag);

        News news = createNews("제목", "미리보기 내용", user, null);
        newsRepository.saveAndFlush(news);

        em.flush();
        em.clear();

        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl("url");
        newsImageRepository.saveAndFlush(newsImage);

        TimelineCard timelineCard = new TimelineCard();
        timelineCard.setNews(news);
        timelineCard.setTitle("제목");
        timelineCard.setContent("내용");
        timelineCard.setSource(List.of("source1", "source2"));
        timelineCard.setStartAt(LocalDate.now());
        timelineCard.setEndAt(LocalDate.now());
        timelineCardRepository.saveAndFlush(timelineCard);

        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag);
        newsTagRepository.saveAndFlush(newsTag);

        // when
        News findedNews = newsRepository.findById(news.getId()).get();
        newsRepository.delete(findedNews);
        em.flush();
        em.clear();

        // then
        assertFalse(newsImageRepository.existsById(newsImage.getId()));
        assertFalse(timelineCardRepository.existsById(timelineCard.getId()));
        assertFalse(newsTagRepository.existsById(newsTag.getId()));
    }

    @Test
    void 입력_키워드_목록과_일치하는_뉴스_조회_성공_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        newsRepository.saveAndFlush(news);
        em.clear();

        NewsTag newsTag1 = createNewsTag(news, tag1);
        newsTagRepository.saveAndFlush(newsTag1);
        NewsTag newsTag2 = createNewsTag(news, tag2);
        newsTagRepository.saveAndFlush(newsTag2);
        NewsTag newsTag3 = createNewsTag(news, tag3);
        newsTagRepository.saveAndFlush(newsTag3);
        em.clear();

        // when
        List<String> keywords1 = List.of(tag1.getName(), tag2.getName(), tag3.getName());
        News findNews1 = newsRepository.findNewsByExactlyMatchingTags(keywords1, keywords1.size()).get();
        List<String> keywords2 = List.of(tag2.getName(), tag1.getName(), tag3.getName());
        News findNews2 = newsRepository.findNewsByExactlyMatchingTags(keywords2, keywords2.size()).get();

        // then
        assertEquals(news.getId(), findNews1.getId());
        assertEquals(newsTag1.getId(), newsTagRepository.findByNewsId(findNews1.getId()).get(0).getId());
        assertEquals(newsTag2.getId(), newsTagRepository.findByNewsId(findNews1.getId()).get(1).getId());
        assertEquals(newsTag3.getId(), newsTagRepository.findByNewsId(findNews1.getId()).get(2).getId());

        assertEquals(news.getId(), findNews2.getId());
        assertEquals(newsTag1.getId(), newsTagRepository.findByNewsId(findNews2.getId()).get(0).getId());
        assertEquals(newsTag2.getId(), newsTagRepository.findByNewsId(findNews2.getId()).get(1).getId());
        assertEquals(newsTag3.getId(), newsTagRepository.findByNewsId(findNews2.getId()).get(2).getId());
    }

    @Test
    void 입력_키워드_목록과_일치하는_뉴스가_없는_경우_조회_검증() {
        // given
        List<String> keywords = List.of(tag1.getName(), tag2.getName(), tag3.getName());

        // when
        News findNews = newsRepository.findNewsByExactlyMatchingTags(keywords, keywords.size()).orElse(null);

        // then
        assertNull(findNews);
    }

    @Test
    void 키워드_기반_뉴스_목록_검색_결과_조회_검증() {
        // given
        News news1 = createNews("제목1", "미리보기 내용1", user, category);
        newsRepository.saveAndFlush(news1);

        News news2 = createNews("제목2", "미리보기 내용2", user, null);
        newsRepository.saveAndFlush(news2);

        News news3 = createNews("제목3", "미리보기 내용3", user, category);
        newsRepository.saveAndFlush(news3);

        News news4 = createNews("제목4", "미리보기 내용4", user, null);
        newsRepository.saveAndFlush(news4);

        em.clear();

        Tag tag4 = new Tag();
        tag4.setName("태그4");
        tagRepository.saveAndFlush(tag4);
        em.clear();

        // news1 가중치: 3
        NewsTag newsTag1 = createNewsTag(news1, tag1);
        newsTagRepository.saveAndFlush(newsTag1);
        NewsTag newsTag2 = createNewsTag(news1, tag2);
        newsTagRepository.saveAndFlush(newsTag2);
        NewsTag newsTag3 = createNewsTag(news1, tag3);
        newsTagRepository.saveAndFlush(newsTag3);

        // news2 가중치: 2
        NewsTag newsTag4 = createNewsTag(news2, tag1);
        newsTagRepository.saveAndFlush(newsTag4);
        NewsTag newsTag5 = createNewsTag(news2, tag2);
        newsTagRepository.saveAndFlush(newsTag5);

        // news3 가중치: 2
        NewsTag newsTag6 = createNewsTag(news3, tag1);
        newsTagRepository.saveAndFlush(newsTag6);
        NewsTag newsTag7 = createNewsTag(news3, tag2);
        newsTagRepository.saveAndFlush(newsTag7);
        NewsTag newsTag8 = createNewsTag(news3, tag4);
        newsTagRepository.saveAndFlush(newsTag8);

        // news4 가중치: 0
        NewsTag newsTag9 = createNewsTag(news4, tag4);
        newsTagRepository.saveAndFlush(newsTag9);

        em.clear();

        // when
        List<String> keywords = List.of(tag1.getName(), tag2.getName(), tag3.getName());
        Page<News> newsPage = newsRepository.searchNewsPageByTags(keywords, PageRequest.of(0, NewsServiceConstant.PAGE_SIZE));
        List<News> newsList = newsPage.getContent();

        // then
        assertEquals(3, newsList.size());
        assertEquals(news1.getId(), newsList.get(0).getId());
        assertEquals(news3.getId(), newsList.get(1).getId());
        assertEquals(news2.getId(), newsList.get(2).getId());
    }
}
