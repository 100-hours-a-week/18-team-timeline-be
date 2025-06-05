package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.Vote;
import com.tamnara.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserIdAndPollId(Long userId, Long pollId);
    List<Vote> findByPollId(Long pollId);
    long countByPollIdAndOptionId(Long pollId, Long optionId);
}
