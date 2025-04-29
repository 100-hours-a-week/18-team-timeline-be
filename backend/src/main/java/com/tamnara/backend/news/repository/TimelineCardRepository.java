package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.TimelineCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineCardRepository extends JpaRepository<TimelineCard, Long> {
}
