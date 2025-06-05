package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, NewsSearchRepository {
    Page<News> findAllByIsHotissueTrueOrderByIdAsc(Pageable pageable);
    Page<News> findByIsHotissueFalseOrderByUpdatedAtDescIdDesc(Pageable pageable);

    @Query("""
        SELECT n FROM News n
        WHERE n.isHotissue = false
          AND (
              (:categoryId IS NULL AND n.category IS NULL)
              OR (:categoryId IS NOT NULL AND n.category.id = :categoryId)
          )
        ORDER BY n.updatedAt DESC, n.id DESC
    """)
    Page<News> findByIsHotissueFalseAndCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

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

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM News n
        WHERE n.updatedAt < :cutoff
    """)
    void deleteAllOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
