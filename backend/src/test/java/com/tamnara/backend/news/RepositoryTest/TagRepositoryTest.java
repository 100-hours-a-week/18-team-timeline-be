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
public class TagRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private TagRepository tagRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private NewsTagRepository newsTagRepository;

    News news;
    Tag tag1;
    Tag tag2;
    Tag tag3;

    @BeforeEach
    public void setUp() {
        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.save(news);

        tag1 = new Tag();
        tag1.setName("tag1");

        tag2 = new Tag();
        tag2.setName("tag2");

        tag3 = new Tag();
        tag3.setName("tag3");
    }

    @Test
    public void 단일_태그_생성_테스트() {
        // given
        tagRepository.save(tag1);

        // when
        Optional<Tag> findTag = tagRepository.findById(tag1.getId());

        // then
        assertEquals(tag1.getName(), findTag.get().getName());
    }

    @Test
    public void 태그_저장_여부_테스트() {
        // given
        tagRepository.save(tag1);

        // when
        String exist = "tag1";
        Optional<Tag> exists = tagRepository.findByName(exist);

        // then
        assertEquals(exist, exists.get().getName());
    }

    @Test
    public void 동일_이름_태그_중복_저장시_예외_발생_테스트() {
        // given
        tagRepository.save(tag1);

        // when
        Tag duplicateTag = new Tag();
        duplicateTag.setName(tag1.getName());

        // then
        assertThrows(RuntimeException.class, () -> {
            tagRepository.save(duplicateTag);
            tagRepository.flush();
        });
    }

    @Test
    public void 고아상태_일괄_삭제_테스트() {
        // given
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        NewsTag newsTag1 = new NewsTag();
        newsTag1.setNews(news);
        newsTag1.setTag(tag1);

        NewsTag newsTag2 = new NewsTag();
        newsTag2.setNews(news);
        newsTag2.setTag(tag2);

        newsTagRepository.save(newsTag1);
        newsTagRepository.save(newsTag2);

        newsTagRepository.delete(newsTag1);

        em.flush();
        em.clear();

        // when
        tagRepository.deleteAllOrphan();
        em.flush();
        em.clear();

        // then
        assertFalse(tagRepository.existsById(tag1.getId()));
        assertTrue(tagRepository.existsById(tag2.getId()));
    }

    @Test
    public void 고아상태_아닌_태그_삭제시_예외_발생_테스트() {
        // given
        tagRepository.save(tag1);

        // when
        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag1);

        newsTagRepository.save(newsTag);

        em.flush();
        em.clear();

        // then
        assertThrows(RuntimeException.class, () -> {
            tagRepository.delete(tag1);
            tagRepository.flush();
        });
    }
}
