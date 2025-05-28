package com.tamnara.backend.news.repository;

import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaConfig.class)
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
    
    private News createNews(String title, String summary, User user, Category category) {
        News news = new News();
        news.setTitle(title);
        news.setSummary(summary);
        news.setUser(user);
        news.setCategory(category);
        return news;
    }

    User user;
    Category category;

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
    void 뉴스_핫이슈_전환_검증() {
        // given
        News news = createNews("제목", "미리보기 내용", user, category);
        news.setIsHotissue(true);
        newsRepository.save(news);

        // when
        News findNews = newsRepository.findById(news.getId()).get();
        findNews.setIsHotissue(false);
        newsRepository.save(findNews);

        // then
        News updatedNews = newsRepository.findById(news.getId()).get();
        assertFalse(updatedNews.getIsHotissue());
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
    void 수정일자가_기준시간보다_오래된_뉴스_일괄_삭제_검증() {
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
}
