package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.VoteStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface VoteStatisticsRepository extends JpaRepository<VoteStatistics, Long> {
    List<VoteStatistics> findByPollId(Long pollId);
    Optional<VoteStatistics> findByPollIdAndOptionId(Long pollId, Long optionId);
}
