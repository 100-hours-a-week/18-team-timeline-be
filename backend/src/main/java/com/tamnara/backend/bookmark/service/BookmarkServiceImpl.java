package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
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
    public Long addBookmark(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 뉴스입니다."));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "뉴스에 이미 북마크가 등록되어 있습니다.");
        }

        Bookmark savedBookmark = new Bookmark();
        savedBookmark.setUser(user);
        savedBookmark.setNews(news);
        bookmarkRepository.save(savedBookmark);
        return savedBookmark.getId();
    }

    @Override
    public Long deleteBookmark(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 뉴스입니다."));

        Optional<Bookmark> bookmark = bookmarkRepository.findByUserAndNews(user, news);
        if (bookmark.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "뉴스에 북마크가 등록되어 있지 않습니다.");
        }

        bookmarkRepository.delete(bookmark.get());
        return bookmark.get().getId();
    }
}
