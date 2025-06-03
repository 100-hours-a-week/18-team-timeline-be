package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.constant.BookmarkServiceConstant;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.dto.response.BookmarkListResponse;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsImage;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.repository.NewsImageRepository;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final NewsImageRepository newsImageRepository;

    @Override
    public BookmarkAddResponse save(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        bookmarkRepository.findByUserAndNews(user, news)
                .ifPresent(b -> { throw new ResponseStatusException(HttpStatus.CONFLICT, BookmarkResponseMessage.BOOKMARK_ALREADY_ADDED); });

        Bookmark savedBookmark = new Bookmark();
        savedBookmark.setUser(user);
        savedBookmark.setNews(news);
        bookmarkRepository.save(savedBookmark);

        return new BookmarkAddResponse(savedBookmark.getId());
    }

    @Override
    public void delete(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        News news  = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.NEWS_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findByUserAndNews(user, news)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, BookmarkResponseMessage.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

    @Override
    public BookmarkListResponse findByUserId(Long userId, Integer offset) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));

        int page = offset / BookmarkServiceConstant.PAGE_SIZE;
        int nextOffset = offset + BookmarkServiceConstant.PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, BookmarkServiceConstant.PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUser(user, pageable);
        List<Bookmark> bookmarkList = bookmarkPage.getContent();

        boolean hasNext = bookmarkRepository.findByUser(user, pageable).hasNext();

        List<NewsCardDTO> newsCardDTOList = new ArrayList<>();
        for (Bookmark b : bookmarkList) {
            News news = newsRepository.findById(b.getNews().getId()).orElse(null);
            if (news == null) { continue; }

            NewsImage image = newsImageRepository.findByNewsId(b.getNews().getId()).orElse(null);

            NewsCardDTO newsCardDTO = new NewsCardDTO(
                    news.getId(),
                    news.getTitle(),
                    news.getSummary(),
                    image != null ? image.getUrl() : null,
                    news.getCategory() != null ? news.getCategory().toString() : null,
                    news.getUpdatedAt(),
                    true,
                    b.getCreatedAt()
            );
            newsCardDTOList.add(newsCardDTO);
        }

        return new BookmarkListResponse(newsCardDTOList, nextOffset, hasNext);
    }
}
