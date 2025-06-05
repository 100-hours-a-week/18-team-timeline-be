package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    List<PollOption> findByPollId(Long pollId);
}
