package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;

public interface BookmarkService {
    BookmarkAddResponse addBookmark(Long userId, Long newsId);
    void deleteBookmark(Long userId, Long newsId);
}
