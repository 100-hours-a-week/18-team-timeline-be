package com.tamnara.backend.poll.scheduler;

import com.tamnara.backend.poll.service.VoteStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteStatisticsScheduler {

    private final VoteStatisticsService voteStatisticsService;

    @Scheduled(cron = "0 */10 * * * *")
    public void generateVoteStatistics() {
        log.info("[스케줄러] 투표 통계 자동 집계를 시작합니다.");
        voteStatisticsService.generateAllStatistics();
        log.info("[스케줄러] 투표 통계 자동 집계가 완료되었습니다.");
    }
}
