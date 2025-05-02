package com.tamnara.backend.news.RepositoryTest;

import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.repository.CategoryRepository;
import com.tamnara.backend.news.repository.NewsImageRepository;
import com.tamnara.backend.news.repository.NewsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class NewsImageRepositoryTest {

    @Autowired private NewsImageRepository newsImageRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager em;

    News news;
    NewsImage newsImage1;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category = new Category();
        category.setName(CategoryType.KTB);
        category.setNum(4L);
        categoryRepository.save(category);

        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        news.setCategory(category);
        newsRepository.save(news);

        newsImage1 = new NewsImage();
        newsImage1.setUrl("url1.png");
        newsImage1.setNews(news);
    }

    @Test
    public void 단일_이미지_생성_테스트() {
        // given
        newsImageRepository.save(newsImage1);

        // when
        NewsImage findNewsImage = newsImageRepository.findById(newsImage1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스 이미지를 찾을 수 없습니다."));

        // then
        assertEquals(newsImage1.getUrl(), findNewsImage.getUrl());
    }

    @Test
    public void 단일_이미지_업데이트_테스트() {
        // given
        newsImageRepository.save(newsImage1);

        // when
        newsImage1.setUrl("url2.png");
        newsImageRepository.save(newsImage1);

        // then
        NewsImage findNewsImage = newsImageRepository.findById(newsImage1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스 이미지를 찾을 수 없습니다."));
        assertEquals(newsImage1.getUrl(), findNewsImage.getUrl());
    }

    @Test
    public void 특정뉴스_단일_이미지_조회_테스트() {
        // given
        newsImageRepository.save(newsImage1);
        em.flush();
        em.clear();

        // when
        Optional<NewsImage> findNewsImage = newsImageRepository.findByNewsId(news.getId());

        // then
        assertEquals(newsImage1.getNews().getId(), findNewsImage.get().getNews().getId());
    }

    @Test
    public void 단일_이미지_삭제_테스트() {
        // given
        newsImageRepository.save(newsImage1);

        // when
        NewsImage findNewsImage = newsImageRepository.findById(newsImage1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스 이미지를 찾을 수 없습니다."));
        newsImageRepository.delete(findNewsImage);
        em.flush();
        em.clear();

        // then
        assertFalse(newsImageRepository.existsById(findNewsImage.getId()));
    }
}
