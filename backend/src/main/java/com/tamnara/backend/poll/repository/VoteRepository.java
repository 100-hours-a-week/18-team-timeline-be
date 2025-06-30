package com.tamnara.backend.poll.repository;

import com.tamnara.backend.poll.domain.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByUserIdAndPollId(Long userId, Long pollId);
    List<Vote> findByPollId(Long pollId);
    long countByPollIdAndOptionId(Long pollId, Long optionId);

    @Query("""
    SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END
    FROM Vote v
    WHERE v.user.id = :userId
      AND v.poll.state = 'PUBLISHED'
      AND v.poll.id = (
          SELECT MAX(p.id)
          FROM Poll p
          WHERE p.state = 'PUBLISHED'
      )
    """)
    Boolean hasVotedLatestPublishedPoll(@Param("userId") Long userId);

    @Query("""
    SELECT v.option.id
    FROM Vote v
    WHERE v.user.id = :userId
      AND v.poll.state = 'PUBLISHED'
      AND v.poll.id = (
          SELECT MAX(p.id)
          FROM Poll p
          WHERE p.state = 'PUBLISHED'
      )
    """)
    List<Long> findVotedOptionIdsOfLatestPublishedPoll(@Param("userId") Long userId);
}
