package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PollRepository extends JpaRepository<Poll, Long> {
    @Query("""
    SELECT p FROM Poll p
    WHERE p.state = 'PUBLISHED'
    ORDER BY p.id DESC
    LIMIT 1
    """)
    Optional<Poll> findLatestPollByPublishedPoll();
    List<Poll> findByState(PollState state);
    List<Poll> findByEndAtAfter(LocalDateTime now);
    List<Poll> findByStateAndEndAtBefore(PollState state, LocalDateTime now);
    List<Poll> findByStateAndStartAtBeforeAndEndAtAfter(PollState state, LocalDateTime nowForStart, LocalDateTime nowForEnd);
    boolean existsByState(PollState state);
}
