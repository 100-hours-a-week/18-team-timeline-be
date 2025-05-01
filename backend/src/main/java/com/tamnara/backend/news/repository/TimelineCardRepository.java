package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineCardRepository extends JpaRepository<TimelineCard, Long> {
    @Query("""
        SELECT t FROM TimelineCard t
        WHERE t.news.id = :newsId
          AND (:type IS NULL OR t.type = :type)
        ORDER BY t.startAt DESC
    """)
    List<TimelineCard> findAllByNewsIdAndType(Long newsId, TimelineCardType type);
}
