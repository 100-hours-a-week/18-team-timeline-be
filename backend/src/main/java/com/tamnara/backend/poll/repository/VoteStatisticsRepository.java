package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.VoteStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface VoteStatisticsRepository extends JpaRepository<VoteStatistics, Long> {

    // 특정 Poll에 대한 통계 목록
    List<VoteStatistics> findByPollId(Long pollId);

    // 특정 Poll + Option에 대한 통계 단건
    Optional<VoteStatistics> findByPollIdAndOptionId(Long pollId, Long optionId);
}
