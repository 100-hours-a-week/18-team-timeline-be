package com.tamnara.backend.news.repository;

import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.global.config.QuerydslConfig;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaConfig.class, QuerydslConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TagRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private TagRepository tagRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private NewsTagRepository newsTagRepository;

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tag;
    }

    News news;

    @BeforeEach
    void setUp() {
        newsTagRepository.deleteAll();
        newsRepository.deleteAll();
        tagRepository.deleteAll();

        em.flush();
        em.clear();

        news = new News();
        news.setTitle("뉴스 제목");
        news.setSummary("뉴스 미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.saveAndFlush(news);
    }

    @Test
    void 단일_태그_생성_성공_검증() {
        // given
        Tag tag = createTag("태그");
        tagRepository.save(tag);

        // when
        Optional<Tag> findTag = tagRepository.findById(tag.getId());

        // then
        assertEquals(tag.getName(), findTag.get().getName());
    }

    @Test
    void 태그_이름_null_불가_검증() {
        // given
        Tag tag = new Tag();

        // when
        tag.setName(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            tagRepository.saveAndFlush(tag);
        });
    }

    @Test
    void 태그_이름_유일성_검증() {
        // given
        String tagName = "태그";
        Tag tag1 = createTag(tagName);
        tagRepository.saveAndFlush(tag1);

        // when
        Tag tag2 = createTag(tagName);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            tagRepository.saveAndFlush(tag2);
        });
    }

    @Test
    void 태그_이름_길이_제약_검증() {
        // given
        String tagName = "이태그의글자수는10자를초과합니다.";

        // when
        Tag tag = createTag(tagName);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            tagRepository.saveAndFlush(tag);
        });
    }

    @Test
    void 태그_이름으로_단일_조회_검증() {
        // given
        String name = "태그";
        Tag tag = createTag(name);
        tagRepository.saveAndFlush(tag);

        // when
        Optional<Tag> exists = tagRepository.findByName(name);

        // then
        assertEquals(name, exists.get().getName());
    }

    @Test
    void 고아_상태인_태그들_일괄_삭제_검증() {
        // given
        Tag tag1 = createTag("태그1");
        tagRepository.save(tag1);
        Tag tag2 = createTag("태그2");
        tagRepository.save(tag2);
        Tag tag3 = createTag("태그3");
        tagRepository.save(tag3);

        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag1);
        newsTagRepository.save(newsTag);

        // when
        tagRepository.deleteAllOrphan();
        em.flush();
        em.clear();

        // then
        assertTrue(tagRepository.existsById(tag1.getId()));
        assertFalse(tagRepository.existsById(tag2.getId()));
        assertFalse(tagRepository.existsById(tag3.getId()));
    }

    @Test
    void 고아_상태가_아닌_태그_삭제_불가_검증() {
        // given
        Tag tag = createTag("태그");
        tagRepository.save(tag);

        // when
        NewsTag newsTag = new NewsTag();
        newsTag.setNews(news);
        newsTag.setTag(tag);
        newsTagRepository.save(newsTag);

        em.flush();
        em.clear();

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            tagRepository.delete(tag);
            tagRepository.flush();
        });
    }
}
