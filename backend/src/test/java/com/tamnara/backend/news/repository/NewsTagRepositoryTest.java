package com.tamnara.backend.news.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsTag;
import com.tamnara.backend.news.domain.Tag;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class NewsTagRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private NewsTagRepository newsTagRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private NewsRepository newsRepository;

    private NewsTag createNewsTag(News news, Tag tag) {
        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag);
        return newsTag;
    }

    News news;
    Tag tag1;
    Tag tag2;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        newsRepository.saveAndFlush(news);

        tag1 = new Tag();
        tag1.setName("태그1");
        tagRepository.saveAndFlush(tag1);

        tag2 = new Tag();
        tag2.setName("태그2");
        tagRepository.saveAndFlush(tag2);

        em.clear();
    }

    @Test
    void 뉴스태그_생성_성공_검증() {
        // given
        NewsTag newsTag = createNewsTag(news, tag1);
        newsTagRepository.saveAndFlush(newsTag);
        em.clear();

        // when
        NewsTag findNewsTag = newsTagRepository.findById(newsTag.getId()).get();

        // then
        assertEquals(newsTag.getId(), findNewsTag.getId());
    }

    @Test
    void 뉴스태그_뉴스_필드_null_불가_검증() {
        // given
        NewsTag newsTag = new NewsTag();

        // when
        newsTag.setTag(tag1);

        // then
        assertNull(newsTag.getNews());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsTagRepository.saveAndFlush(newsTag);
        });
    }

    @Test
    void 뉴스태그_태그_필드_null_불가_검증() {
        // given
        NewsTag newsTag = new NewsTag();

        // when
        newsTag.setNews(news);

        // then
        assertNull(newsTag.getTag());
        assertThrows(DataIntegrityViolationException.class, () -> {
            newsTagRepository.saveAndFlush(newsTag);
        });
    }

    @Test
    void 뉴스태그_뉴스와_태그_업데이트_불가_검증() {
        // given
        NewsTag newsTag = createNewsTag(news, tag1);
        newsTagRepository.saveAndFlush(newsTag);
        em.clear();

        // when
        News newNews = new News();
        newNews.setTitle("제목");
        newNews.setSummary("미리보기 요약");
        newsRepository.saveAndFlush(newNews);
        em.clear();

        NewsTag findNewsTag = newsTagRepository.findById(newsTag.getId()).get();
        findNewsTag.setNews(newNews);
        findNewsTag.setTag(tag2);
        newsTagRepository.saveAndFlush(newsTag);
        em.clear();

        // then
        assertEquals(news.getId(), findNewsTag.getNews().getId());
        assertEquals(tag1.getId(), findNewsTag.getTag().getId());
    }

    @Test
    void 뉴스태그_삭제_성공_검증() {
        // given
        NewsTag newsTag = createNewsTag(news, tag1);
        newsTagRepository.saveAndFlush(newsTag);
        em.clear();

        // when
        NewsTag findNewsTag = newsTagRepository.findById(newsTag.getId()).get();
        newsTagRepository.delete(findNewsTag);
        em.flush();
        em.clear();

        // then
        assertFalse(newsTagRepository.existsById(newsTag.getId()));
    }

    @Test
    void 뉴스_ID로_연관된_뉴스태그들_조회_성공_검증() {
        // given
        NewsTag newsTag1 = createNewsTag(news, tag1);
        newsTagRepository.saveAndFlush(newsTag1);
        NewsTag newsTag2 = createNewsTag(news, tag2);
        newsTagRepository.saveAndFlush(newsTag2);
        em.clear();

        // when
        List<NewsTag> findNewsTags = newsTagRepository.findByNewsId(news.getId());

        // then
        assertEquals(2, findNewsTags.size());
    }
}
