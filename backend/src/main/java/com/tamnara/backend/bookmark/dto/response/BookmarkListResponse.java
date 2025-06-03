package com.tamnara.backend.bookmark.dto.response;

import com.tamnara.backend.news.dto.NewsCardDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BookmarkListResponse {
    private List<NewsCardDTO> bookmarks;
    private int offset;
    private boolean hasNext;
}
