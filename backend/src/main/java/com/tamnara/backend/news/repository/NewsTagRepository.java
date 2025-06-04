package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.NewsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsTagRepository extends JpaRepository<NewsTag, Long> {
    List<NewsTag> findByNewsId(Long newsId);

    @Query(value = """
        SELECT n.*
        FROM news n
        JOIN news_tag nt ON n.id = nt.news_id
        JOIN tags t ON nt.tag_id = t.id
        WHERE t.name IN (:keywords)
        GROUP BY n.id
        HAVING 
            COUNT(DISTINCT CASE WHEN t.name IN (:keywords) THEN t.name END) = :size
            AND COUNT(*) = :size
        ORDER BY n.updated_at DESC
        LIMIT 1
    """, nativeQuery = true)
    Optional<News> findNewsByExactlyMatchingTags(@Param("keywords") List<String> keywords, @Param("size") Integer size);
}
