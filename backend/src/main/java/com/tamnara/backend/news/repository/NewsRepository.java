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

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    @Query("""
        SELECT n FROM News n
        WHERE n.isHotissue = :isHotissue
        AND (:isHotissue = true OR n.category.id = :categoryId)
        ORDER BY n.updatedAt DESC
    """)
    Page<News> findNewsByIsHotissueAndCategoryId(
            @Param("isHotissue") boolean isHotissue,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("""
        DELETE FROM News n
        WHERE n.updatedAt < :cutoff
    """)
    void deleteNewsOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
