package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.TimelineCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineCardRepository extends JpaRepository<TimelineCard, Long> {
    List<TimelineCard> findAllByNewsIdOrderByStartAtDesc(Long newsId);
    void deleteAllByNewsId(Long newsId);
}
