package com.tamnara.backend.news.RepositoryTest;

import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.repository.CategoryRepository;
import com.tamnara.backend.news.repository.NewsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NewsRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private NewsRepository newsRepository;
    @Autowired private CategoryRepository categoryRepository;
//    @Autowired private UserRepository userRepository;

    News news1;
    News news2;
    News news3;
    Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName(CategoryType.KTB);
        category.setNum(4L);
        categoryRepository.save(category);

        news1 = new News();
        news1.setTitle("24자의 제목");
        news1.setSummary("36자의 미리보기 내용");
        news1.setIsHotissue(true);
        news1.setCategory(category);

        news2 = new News();
        news2.setTitle("24자의 제목");
        news2.setSummary("36자의 미리보기 내용");
        news2.setIsHotissue(true);
        news2.setCategory(category);

        news3 = new News();
        news3.setTitle("24자의 제목");
        news3.setSummary("36자의 미리보기 내용");
        news3.setIsHotissue(true);
        news3.setCategory(category);
    }

    @Test
    public void 뉴스_생성_테스트() {
        // given
        newsRepository.save(news1);

        // when
        News findNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));

        // then
        assertEquals(news1.getTitle(), findNews.getTitle());
    }

    @Test
    public void 뉴스_업데이트_테스트() {
        // given
        newsRepository.save(news1);

        // when
        News findNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        findNews.setSummary("36자의 업데이트된 미리보기 내용");
        findNews.setViewCount(news1.getViewCount() + 1);
        findNews.setUpdateCount(news1.getUpdateCount() + 1);
        findNews.setRatioPosi(20);
        findNews.setRatioNeut(40);
        findNews.setRatioNega(40);
        newsRepository.save(findNews);

        // then
        News updatedNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        assertEquals(news1.getTitle(), updatedNews.getTitle());
        assertEquals(findNews.getSummary(), updatedNews.getSummary());
        assertEquals(findNews.getIsHotissue(), updatedNews.getIsHotissue());
        assertEquals(findNews.getViewCount(), updatedNews.getViewCount());
        assertEquals(findNews.getUpdateCount(), updatedNews.getUpdateCount());
        assertEquals(findNews.getRatioPosi(), updatedNews.getRatioPosi());
        assertEquals(findNews.getRatioNeut(), updatedNews.getRatioNeut());
        assertEquals(findNews.getRatioNega(), updatedNews.getRatioNega());
        assertEquals(findNews.getCategory(), updatedNews.getCategory());
    }

    @Test
    public void 뉴스_핫이슈_전환_테스트() {
        // given
        news1.setIsHotissue(true);
        newsRepository.save(news1);

        // when
        News findNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        findNews.setIsHotissue(false);
        newsRepository.save(findNews);

        // then
        News updatedNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        assertFalse(updatedNews.getIsHotissue());
    }

    @Test
    public void 뉴스_삭제_테스트() {
        // given
        newsRepository.save(news1);

        // when
        News findNews = newsRepository.findById(news1.getId())
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다."));
        newsRepository.delete(findNews);

        // then
        assertFalse(newsRepository.findById(news1.getId()).isPresent());
    }

    @Test
    public void 뉴스_조건충족_일괄_삭제_테스트() {
        // given
        newsRepository.save(news1);
        newsRepository.save(news2);
        newsRepository.save(news3);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // when
        LocalDateTime cutoff = LocalDateTime.now();
        newsRepository.deleteAllOlderThan(cutoff);

        // then
        List<News> remainList = newsRepository.findAll();
        assertEquals(0, remainList.size());
    }

    @Test
    public void 핫이슈_뉴스_전체_조회_테스트() {
        // given
        news1.setIsHotissue(true);
        news2.setIsHotissue(true);
        news3.setIsHotissue(true);

        newsRepository.save(news1);
        newsRepository.save(news2);
        newsRepository.save(news3);

        // when
        Page<News> hotissueList = newsRepository.findAllByIsHotissue(true, Pageable.unpaged());

        // then
        assertEquals(3, hotissueList.getContent().size());
        assertTrue(hotissueList.stream().allMatch(News::getIsHotissue));
    }

    @Test
    public void 일반_뉴스_전체_페이징_조회_테스트() {
        // given
        news1.setIsHotissue(false);
        news2.setIsHotissue(false);
        news3.setIsHotissue(false);

        newsRepository.save(news3);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news2);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news1);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<News> normalList = newsRepository.findAllByIsHotissue(false, pageable);

        // then
        assertEquals(2, normalList.getContent().size());
        assertFalse(normalList.stream().allMatch(News::getIsHotissue));

        News first = normalList.getContent().get(0);
        News second = normalList.getContent().get(1);
        assertTrue(first.getUpdatedAt().isAfter(second.getUpdatedAt()));
    }

    @Test
    public void 일반_뉴스_카테고리_페이징_조회_테스트() {
        // given
        news1.setIsHotissue(false);
        news2.setIsHotissue(false);
        news3.setIsHotissue(false);

        news1.setCategory(category);
        news2.setCategory(category);
        news3.setCategory(category);

        newsRepository.save(news3);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news2);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news1);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<News> normalCategoryList = newsRepository.findNewsByIsHotissueAndCategoryId(false, category.getId(), pageable);

        // then
        assertEquals(2, normalCategoryList.getContent().size());
        assertFalse(normalCategoryList.stream().allMatch(News::getIsHotissue));
        assertTrue(normalCategoryList.stream().allMatch(news -> news.getCategory().equals(category)));

        News first = normalCategoryList.getContent().get(0);
        News second = normalCategoryList.getContent().get(1);
        assertTrue(first.getUpdatedAt().isAfter(second.getUpdatedAt()));
    }

    @Test
    public void 일반_뉴스_기타_조회_테스트() {
        // given
        news1.setIsHotissue(false);
        news2.setIsHotissue(false);
        news3.setIsHotissue(false);

        news1.setCategory(null);
        news2.setCategory(null);
        news3.setCategory(null);

        newsRepository.save(news3);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news2);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        newsRepository.save(news1);

        // when
        Pageable pageable = PageRequest.of(0, 2);
        Page<News> normalNoCategoryList = newsRepository.findNewsByIsHotissueAndCategoryId(false, null, pageable);

        // then
        assertEquals(2, normalNoCategoryList.getContent().size());
        assertFalse(normalNoCategoryList.stream().allMatch(News::getIsHotissue));
        assertTrue(normalNoCategoryList.stream().allMatch(news -> news.getCategory() == null));

        News first = normalNoCategoryList.getContent().get(0);
        News second = normalNoCategoryList.getContent().get(1);
        assertTrue(first.getUpdatedAt().isAfter(second.getUpdatedAt()));
    }
}
