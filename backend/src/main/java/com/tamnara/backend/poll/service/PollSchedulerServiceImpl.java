package com.tamnara.backend.poll.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PollSchedulerServiceImpl implements PollSchedulerService {

    private final PollService pollService;

    public PollSchedulerServiceImpl(PollService pollService) {
        this.pollService = pollService;
    }

    @Override
    @Async
    @Scheduled(cron = "0 0 10 * * MON")
    public void updatePollStates() {
        log.info("[POLL] updatePollStates 시작");
        Long start = System.currentTimeMillis();

        pollService.updatePollStates();

        Long end = System.currentTimeMillis();
        log.info("[POLL] updatePollStates 완료 - 소요 시간:{}ms", end - start);
    }
}
