package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NewsSearchRepository {
    Page<News> searchNewsPageByTags(List<String> keywords, Pageable pageable);
}
