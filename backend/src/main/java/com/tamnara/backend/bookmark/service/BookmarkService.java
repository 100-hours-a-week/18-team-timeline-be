package com.tamnara.backend.bookmark.service;

import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;

public interface BookmarkService {
    BookmarkAddResponse save(Long userId, Long newsId);
    void delete(Long userId, Long newsId);
}
