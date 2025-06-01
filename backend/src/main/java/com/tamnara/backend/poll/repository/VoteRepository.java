package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.Vote;
import com.tamnara.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 특정 유저가 특정 투표에 대해 남긴 모든 투표 기록
    List<Vote> findByUserIdAndPollId(Long userId, Long pollId);

    // 특정 Poll의 모든 투표 기록
    List<Vote> findByPollId(Long pollId);
}
