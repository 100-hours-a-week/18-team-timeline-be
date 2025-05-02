package com.tamnara.backend.bookmark.RepositoryTest;

import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.news.domain.News;
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
public class BookmarkRepositoryTest {

    @PersistenceContext private EntityManager em;

    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private NewsRepository newsRepository;
//    @Autowired private UserRepository userRepository;

    News news;
    Bookmark bookmark1;
    Bookmark bookmark2;
    Bookmark bookmark3;

    @BeforeEach
    void setUp() {
        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.save(news);

        bookmark1 = new Bookmark();
        bookmark1.setNews(news);

        bookmark2 = new Bookmark();
        bookmark2.setNews(news);

        bookmark3 = new Bookmark();
        bookmark3.setNews(news);
    }

    @Test
    public void 단일_북마크_생성_테스트() {
        // given
        bookmarkRepository.save(bookmark1);

        // when
        Optional<Bookmark> findBookmark = bookmarkRepository.findById(bookmark1.getId());

        // then
        assertEquals(bookmark1.getId(), findBookmark.get().getId());
    }

    @Test
    public void 단일_북마크_삭제_테스트() {
        // given
        bookmarkRepository.save(bookmark1);

        // when
        Optional<Bookmark> findBookmark = bookmarkRepository.findById(bookmark1.getId());
        bookmarkRepository.delete(findBookmark.get());

        // then
        assertFalse(bookmarkRepository.findById(bookmark1.getId()).isPresent());
    }

    @Test
    public void 뉴스_삭제시_연관_북마크들_자동_삭제_테스트() {
        // given
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);
        bookmarkRepository.save(bookmark3);

        em.flush();
        em.clear();

        // when
        newsRepository.delete(news);

        em.flush();
        em.clear();

        // then
        assertFalse(bookmarkRepository.findById(bookmark1.getId()).isPresent());
        assertFalse(bookmarkRepository.findById(bookmark2.getId()).isPresent());
        assertFalse(bookmarkRepository.findById(bookmark3.getId()).isPresent());
    }
}
