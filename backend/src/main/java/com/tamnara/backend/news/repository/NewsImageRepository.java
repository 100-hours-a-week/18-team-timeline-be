package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.NewsImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NewsImageRepository extends JpaRepository<NewsImage, Long> {
    Optional<NewsImage> findByNewsId(Long newsId);
}
