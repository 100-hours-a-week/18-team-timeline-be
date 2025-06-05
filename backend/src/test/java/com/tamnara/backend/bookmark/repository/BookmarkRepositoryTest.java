package com.tamnara.backend.bookmark.repository;

import com.tamnara.backend.bookmark.constant.BookmarkServiceConstant;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookmarkRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private UserRepository userRepository;

    User user;
    News news;

    @BeforeEach
    void setUp() {
        newsRepository.deleteAll();
        userRepository.deleteAll();

        em.flush();
        em.clear();

        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        news.setIsHotissue(true);
        newsRepository.saveAndFlush(news);

        this.user = User.builder()
                .email("이메일")
                .password("비밀번호")
                .username("이름")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(this.user);
    }

    private Bookmark createBookmark(News news, User user) {
        Bookmark bookmark = new Bookmark();
        bookmark.setNews(news);
        bookmark.setUser(user);
        return bookmark;
    }

    @Test
    void 북마크_생성_성공_검증() {
        // given
        Bookmark bookmark = createBookmark(news, user);

        // when
        bookmarkRepository.saveAndFlush(bookmark);
        Bookmark findedBookmark = bookmarkRepository.findById(bookmark.getId()).get();

        // then
        assertEquals(bookmark.getId(), findedBookmark.getId());
    }

    @Test
    void 북마크_뉴스_필드_null_불가_검증() {
        // given
        Bookmark bookmark = createBookmark(null, user);

        // when & then
        assertNull(bookmark.getNews());
        assertThrows(DataIntegrityViolationException.class, () -> {
            bookmarkRepository.saveAndFlush(bookmark);
        });
    }

    @Test
    void 북마크_회원_필드_null_불가_검증() {
        // given
        Bookmark bookmark = createBookmark(news, null);

        // when & then
        assertNull(bookmark.getUser());
        assertThrows(DataIntegrityViolationException.class, () -> {
            bookmarkRepository.saveAndFlush(bookmark);
        });
    }

    @Test
    void 북마크_수정_불가_검증() {
        // given
        Bookmark bookmark = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark);

        // when
        News news2 = new News();
        news2.setTitle("제목");
        news2.setSummary("미리보기 내용");
        news2.setIsHotissue(true);
        newsRepository.saveAndFlush(news2);

        User user2 = User.builder()
                .email("이메일2")
                .password("비밀번호2")
                .username("이름2")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(user2);

        Bookmark findedBookmark = bookmarkRepository.findById(bookmark.getId()).get();
        findedBookmark.setNews(newsRepository.findById(news2.getId()).get());
        findedBookmark.setUser(userRepository.findById(user2.getId()).get());
        bookmarkRepository.saveAndFlush(findedBookmark);

        // then
        assertEquals(user2, findedBookmark.getUser());
        assertEquals(news2, findedBookmark.getNews());
    }

    @Test
    void 북마크_삭제_성공_검증() {
        // given
        Bookmark bookmark = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark);
        em.clear();

        // when
        Bookmark findedBookmark = bookmarkRepository.findById(bookmark.getId()).get();
        bookmarkRepository.delete(findedBookmark);

        // then
        assertFalse(bookmarkRepository.existsById(findedBookmark.getId()));
    }

    @Test
    void 회원과_뉴스로_단일_북마크_조회_검증() {
        // given
        Bookmark bookmark = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark);
        em.clear();

        // when
        Bookmark findedBookmark = bookmarkRepository.findByUserAndNews(user, news).get();

        // then
        assertEquals(bookmark.getId(), findedBookmark.getId());
    }

    @Test
    void 회원으로_북마크_목록_조회_검증() {
        // given
        Bookmark bookmark1 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark1);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        Bookmark bookmark2 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark2);
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        Bookmark bookmark3 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark3);
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, BookmarkServiceConstant.PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUser(user, pageable);
        List<Bookmark> bookmarkList = bookmarkPage.getContent();

        // then
        assertEquals(3, bookmarkList.size());
        assertEquals(bookmark3, bookmarkList.get(0));
        assertEquals(bookmark2, bookmarkList.get(1));
        assertEquals(bookmark1, bookmarkList.get(2));
    }

    @Test
    void 뉴스_삭제_시_연관된_북마크_CASCADE_검증() {
        // given
        Bookmark bookmark1 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark1);
        Bookmark bookmark2 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark2);
        Bookmark bookmark3 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark3);
        em.clear();

        // when
        newsRepository.delete(news);
        em.flush();
        em.clear();

        // then
        assertFalse(bookmarkRepository.existsById(bookmark1.getId()));
        assertFalse(bookmarkRepository.existsById(bookmark2.getId()));
        assertFalse(bookmarkRepository.existsById(bookmark3.getId()));
    }

    @Test
    void 회원_삭제_시_연관된_북마크들_CASCADE_검증() {
        // given
        Bookmark bookmark1 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark1);
        Bookmark bookmark2 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark2);
        Bookmark bookmark3 = createBookmark(news, user);
        bookmarkRepository.saveAndFlush(bookmark3);
        em.clear();

        // when
        userRepository.delete(user);
        em.flush();
        em.clear();

        // then
        assertFalse(bookmarkRepository.existsById(bookmark1.getId()));
        assertFalse(bookmarkRepository.existsById(bookmark2.getId()));
        assertFalse(bookmarkRepository.existsById(bookmark3.getId()));
    }
}
