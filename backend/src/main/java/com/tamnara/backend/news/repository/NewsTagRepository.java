package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.NewsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsTagRepository extends JpaRepository<NewsTag, Long> {
    List<NewsTag> findByNewsId(Long newsId);
}
