package com.tamnara.backend.news.repository;

import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsImageRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private NewsImageRepository newsImageRepository;
    @Autowired private NewsRepository newsRepository;

    private NewsImage createNewsImage(News news, String url) {
        NewsImage newsImage = new NewsImage();
        newsImage.setNews(news);
        newsImage.setUrl(url);
        return newsImage;
    }

    News news;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        newsRepository.save(news);
    }

    @Test
    void 이미지_생성_성공_검증() {
        // given
        NewsImage newsImage = createNewsImage(news, "url");
        newsImageRepository.saveAndFlush(newsImage);

        // when
        NewsImage findNewsImage = newsImageRepository.findById(newsImage.getId()).get();

        // then
        assertEquals(newsImage.getId(), findNewsImage.getId());
    }

    @Test
    void 이미지_뉴스_필드_null_불가_검증() {
        // given
        NewsImage newsImage = new NewsImage();

        // when
        newsImage.setUrl("url");

        // when & then
        assertNull(newsImage.getNews());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsImageRepository.saveAndFlush(newsImage);
        });
    }

    @Test
    void 이미지_URL_필드_null_불가_검증() {
        // given
        NewsImage newsImage = new NewsImage();

        // when
        newsImage.setNews(news);

        // then
        assertNull(newsImage.getUrl());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsImageRepository.saveAndFlush(newsImage);
        });
    }

    @Test
    void 이미지_URL_필드_길이_제약_검증() {
        // given
        String url = "u".repeat(256);

        // when
        NewsImage newsImage = createNewsImage(news, url);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsImageRepository.saveAndFlush(newsImage);
        });
    }

    @Test
    void 이미지_업데이트_시_URL만_수정_가능_검증() {
        // given
        NewsImage newsImage = createNewsImage(news, "url");
        newsImageRepository.saveAndFlush(newsImage);

        // when
        News newNews = new News();
        newNews.setTitle("제목");
        newNews.setSummary("미리보기 내용");

        NewsImage findNewsImage = newsImageRepository.findById(newsImage.getId()).get();
        findNewsImage.setNews(newNews);
        findNewsImage.setUrl("newUrl");

        // then
        assertEquals(newsImage.getNews(), findNewsImage.getNews());
        assertEquals("newUrl", findNewsImage.getUrl());
    }

    @Test
    void 이미지_삭제_성공_검증() {
        // given
        NewsImage newsImage = createNewsImage(news, "url");
        newsImageRepository.saveAndFlush(newsImage);

        // when
        NewsImage findNewsImage = newsImageRepository.findById(newsImage.getId()).get();
        newsImageRepository.delete(findNewsImage);
        em.flush();
        em.clear();

        // then
        assertFalse(newsImageRepository.existsById(findNewsImage.getId()));
    }

    @Test
    void 뉴스_ID로_이미지_조회_검증() {
        // given
        NewsImage newsImage = createNewsImage(news, "url");
        newsImageRepository.saveAndFlush(newsImage);

        // when
        NewsImage findNewsImage = newsImageRepository.findByNewsId(news.getId()).get();

        // then
        assertEquals(newsImage.getId(), findNewsImage.getId());
        assertEquals(newsImage.getNews().getId(), findNewsImage.getNews().getId());
    }
}
