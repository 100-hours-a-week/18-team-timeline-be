package com.tamnara.backend.news.RepositoryTest;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsTag;
import com.tamnara.backend.news.domain.Tag;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.news.repository.NewsTagRepository;
import com.tamnara.backend.news.repository.TagRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NewsTagRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private NewsTagRepository newsTagRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private NewsRepository newsRepository;

    News news;
    Tag tag;
    NewsTag newsTag1;
    NewsTag newsTag2;

    @BeforeEach
    public void setUp() {
        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.save(news);

        tag = new Tag();
        tag.setName("test");
        tagRepository.save(tag);

        newsTag1 = new NewsTag();
        newsTag1.setNews(news);
        newsTag1.setTag(tag);

        newsTag2 = new NewsTag();
        newsTag2.setNews(news);
        newsTag2.setTag(tag);
    }

    @Test
    public void 뉴스태그_단일_생성_테스트() {
        // given
        newsTagRepository.save(newsTag1);

        // when
        Optional<NewsTag> findNewsTag = newsTagRepository.findById(newsTag1.getId());

        // then
        assertEquals(newsTag1.getTag().getId(), findNewsTag.get().getTag().getId());
    }

    @Test
    public void 뉴스_삭제시_연관_뉴스태그_자동_삭제_테스트() {
        // given
        newsTagRepository.save(newsTag1);
        newsTagRepository.save(newsTag2);

        em.flush();
        em.clear();

        // when
        Optional<News> savedNews = newsRepository.findById(news.getId());
        newsRepository.delete(savedNews.get());

        em.flush();
        em.clear();

        // then
        assertFalse(newsRepository.findById(news.getId()).isPresent());
        assertTrue(tagRepository.findById(tag.getId()).isPresent());
        assertEquals(0, newsTagRepository.findAll().size());
    }
}
