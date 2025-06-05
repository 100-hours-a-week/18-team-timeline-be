package com.tamnara.backend.poll.service;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollOption;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.domain.VoteStatistics;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.repository.VoteRepository;
import com.tamnara.backend.poll.repository.VoteStatisticsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.List;

import static com.tamnara.backend.poll.util.VoteStatisticsBuilder.buildNew;
import static com.tamnara.backend.poll.util.VoteStatisticsBuilder.buildUpdated;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteStatisticsService {

    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final VoteStatisticsRepository voteStatisticsRepository;

    @Transactional
    public void generateAllStatistics() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Poll> polls = pollRepository.findByState(PollState.PUBLISHED);

        for (Poll poll : polls) {
            for (PollOption option : poll.getOptions()) {
                long count = voteRepository.countByPollIdAndOptionId(poll.getId(), option.getId());

                VoteStatistics updated = voteStatisticsRepository.findByPollIdAndOptionId(poll.getId(), option.getId())
                        .map(existing -> buildUpdated(existing, count))
                        .orElseGet(() -> buildNew(poll, option, count));

                voteStatisticsRepository.save(updated);
            }
        }
        stopWatch.stop();
        log.info("[통계 집계 완료] 총 소요 시간: {} ms", stopWatch.getTotalTimeMillis());
    }
}
