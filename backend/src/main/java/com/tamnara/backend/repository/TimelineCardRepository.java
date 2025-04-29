package com.tamnara.backend.repository;

import com.tamnara.backend.domain.TimelineCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineCardRepository extends JpaRepository<TimelineCard, Long> {
}
