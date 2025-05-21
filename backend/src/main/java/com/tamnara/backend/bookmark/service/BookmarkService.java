package com.tamnara.backend.bookmark.service;

public interface BookmarkService {
    Long addBookmark(Long userId, Long newsId);
    void deleteBookmark(Long userId, Long newsId);
}
