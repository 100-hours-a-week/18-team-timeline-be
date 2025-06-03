package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.dto.response.BookmarkListResponse;

public interface BookmarkService {
    BookmarkAddResponse save(Long userId, Long newsId);
    void delete(Long userId, Long newsId);
    BookmarkListResponse findByUserId(Long userId, Integer offset);
}
