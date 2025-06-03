package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.constant.BookmarkServiceConstant;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.dto.response.BookmarkListResponse;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.repository.NewsImageRepository;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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
    @Mock private NewsImageRepository newsImageRepository;

    @InjectMocks private BookmarkServiceImpl bookmarkServiceImpl;

    User user;
    News news;

    @BeforeEach
    void setUp() {
        long USER_ID = 1L;
        long NEWS_ID = 1L;

        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(USER_ID);

        news = mock(News.class);
        lenient().when(news.getId()).thenReturn(NEWS_ID);
    }

    private Bookmark createBookmark(Long id, User user, News news) {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(id);
        bookmark.setUser(user);
        bookmark.setNews(news);
        return bookmark;
    }

    @Test
    void 북마크_저장_검증() {
        // given
        Bookmark bookmark = createBookmark(null, user, news);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(newsRepository.findById(news.getId())).thenReturn(Optional.of(news));
        when(bookmarkRepository.findByUserAndNews(user, news)).thenReturn(Optional.empty());

        // when
        BookmarkAddResponse response = bookmarkServiceImpl.save(user.getId(), news.getId());

        // then
        assertEquals(response.getBookmarkId(), bookmark.getId());
    }

    @Test
    void 동일한_북마크가_존재할_경우_저장_예외_처리_검증() {
        // given
        Bookmark bookmark = createBookmark(null , user, news);
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
        Bookmark bookmark = createBookmark(1L, user, news);
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

    @Test
    void 회원이_북마크한_뉴스_카드_목록_조회_검증() {
        // given
        Long newsId = news.getId();
        News news1 = mock(News.class);
        when(news1.getId()).thenReturn(newsId + 1);
        News news2 = mock(News.class);
        when(news2.getId()).thenReturn(newsId + 2);
        News news3 = mock(News.class);
        when(news3.getId()).thenReturn(newsId + 3);

        Category category = mock(Category.class);
        category.setName(CategoryType.ECONOMY);
        when(news1.getCategory()).thenReturn(category);
        when(news2.getCategory()).thenReturn(null);
        when(news3.getCategory()).thenReturn(category);

        NewsImage newsImage1 = mock(NewsImage.class);
        NewsImage newsImage2 = mock(NewsImage.class);

        Bookmark bookmark1 = createBookmark(1L, user, news1);
        Bookmark bookmark2 = createBookmark(2L, user, news2);
        Bookmark bookmark3 = createBookmark(3L, user, news3);
        List<Bookmark> bookmarkList = List.of(bookmark3, bookmark2, bookmark1);

        Pageable pageable = PageRequest.of(0, BookmarkServiceConstant.PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bookmark> bookmarkPage = new PageImpl<>(bookmarkList, pageable, bookmarkList.size());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        when(newsRepository.findById(news1.getId())).thenReturn(Optional.of(news1));
        when(newsRepository.findById(news2.getId())).thenReturn(Optional.of(news2));
        when(newsRepository.findById(news3.getId())).thenReturn(Optional.of(news3));

        when(newsImageRepository.findByNewsId((news1.getId()))).thenReturn(Optional.of(newsImage1));
        when(newsImageRepository.findByNewsId((news2.getId()))).thenReturn(Optional.of(newsImage2));
        when(newsImageRepository.findByNewsId((news3.getId()))).thenReturn(Optional.empty());

        when(bookmarkRepository.findByUser(user, pageable)).thenReturn(bookmarkPage);

        // when
        BookmarkListResponse response = bookmarkServiceImpl.getBookmarkedNewsList(user.getId(), 0);

        // then
        assertEquals(bookmarkList.size(), response.getBookmarks().size());
        assertEquals(news3.getId(), response.getBookmarks().get(0).getId());
        assertEquals(news2.getId(), response.getBookmarks().get(1).getId());
        assertEquals(news1.getId(), response.getBookmarks().get(2).getId());
        assertEquals(BookmarkServiceConstant.PAGE_SIZE, response.getOffset());
        assertFalse(response.isHasNext());
    }

    @Test
    void 북마크한_뉴스_카드_목록_조회_시_회원이_존재하지_않으면_예외_처리_검증() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            bookmarkServiceImpl.getBookmarkedNewsList(user.getId(), 0);
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ResponseMessage.USER_NOT_FOUND, exception.getReason());;
    }
}
