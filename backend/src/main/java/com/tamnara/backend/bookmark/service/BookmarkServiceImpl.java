package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    @Override
    public BookmarkAddResponse addBookmark(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, BookmarkResponseMessage.BOOKMARK_ALREADY_ADDED);
        }

        Bookmark savedBookmark = new Bookmark();
        savedBookmark.setUser(user);
        savedBookmark.setNews(news);
        bookmarkRepository.save(savedBookmark);

        return new BookmarkAddResponse(savedBookmark.getId());
    }

    @Override
    public void deleteBookmark(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, BookmarkResponseMessage.BOOKMARK_NOT_FOUND);
        }

        bookmarkRepository.delete(bookmark.get());
    }
}
