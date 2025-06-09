package com.tamnara.backend.poll.scheduler;

import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.repository.PollRepository;
import com.tamnara.backend.poll.service.PollService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollStateScheduler {

    private final PollRepository pollRepository;
    private final PollService pollService;

    @Scheduled(cron = "0 5/10 * * * *")
    public void updatePollStates() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[스케줄러] 투표 상태 자동 전환을 시작합니다.");

        // PUBLISHED → DELETED (endAt 지난 투표 모두 삭제 처리)
        List<Poll> toDelete = pollRepository.findByStateAndEndAtBefore(PollState.PUBLISHED, now);
        if (!toDelete.isEmpty()) {
            toDelete.forEach(pollService::deletePoll);
            log.info("[스케줄러] {}개의 투표를 DELETED 처리했습니다.", toDelete.size());
        }

        // SCHEDULED → PUBLISHED (가장 이른 startAt 우선)
        List<Poll> scheduled = pollRepository
                .findByStateAndStartAtBeforeAndEndAtAfter(PollState.SCHEDULED, now, now);

        scheduled.stream()
                .min(Comparator.comparing(Poll::getStartAt))
                .ifPresent(poll -> {
                    pollService.publishPoll(poll);
                    log.info("[스케줄러] 투표(ID={})를 PUBLISHED 처리했습니다.", poll.getId());
                });

        log.info("[스케줄러] 투표 상태 자동 전환을 완료했습니다.");
    }
}
