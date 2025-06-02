package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookmarkServiceImplTest {

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private UserRepository userRepository;
    @Mock private NewsRepository newsRepository;

    @InjectMocks private BookmarkServiceImpl bookmarkServiceImpl;

    User user;
    News news;

    @BeforeEach
    void setUp() {
        Long USER_ID = 1L;
        Long NEWS_ID = 1L;

        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(USER_ID);

        news = mock(News.class);
        lenient().when(news.getId()).thenReturn(NEWS_ID);
    }

    private Bookmark createBookmark(User user, News news) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNews(news);
        return bookmark;
    }

    @Test
    void 북마크_저장_검증() {
        // given
        Bookmark bookmark = createBookmark(user, news);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.of(bookmark));

        // when
        BookmarkAddResponse response = bookmarkServiceImpl.save(user.getId(), news.getId());

        // then
        assertEquals(response.getBookmarkId(), bookmark.getId());
    }

    @Test
    void 동일한_북마크가_존재할_경우_저장_예외_처리_검증() {
        // given
        Bookmark bookmark = createBookmark(user, news);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.of(bookmark));

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            bookmarkServiceImpl.save(user.getId(), news.getId());
        });

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(BookmarkResponseMessage.BOOKMARK_ALREADY_ADDED, exception.getReason());
    }

    @Test
    void 북마크_삭제_검증() {
        // given
        Bookmark bookmark = createBookmark(user, news);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.of(bookmark));

        // when
        bookmarkServiceImpl.delete(user.getId(), news.getId());

        // then
        assertFalse(bookmarkRepository.existsById(bookmark.getId()));
    }

    @Test
    void 북마크가_존재하지_않을_경우_삭제_예외_처리_검증() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            bookmarkServiceImpl.delete(user.getId(), news.getId());
        });

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(BookmarkResponseMessage.BOOKMARK_NOT_FOUND, exception.getReason());
    }
}
