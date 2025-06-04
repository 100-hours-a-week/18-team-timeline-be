package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Long> {
    // 상태로 투표 필터링
    List<Poll> findByState(PollState state);

    // 종료일 이전의 투표만 찾기
    List<Poll> findByEndAtAfter(LocalDateTime now);
}
