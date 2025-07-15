package com.tamnara.backend.alarm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlarmSchedulerServiceImpl implements AlarmSchedulerService {

    private final AlarmService alarmService;

    public AlarmSchedulerServiceImpl(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @Override
    @Async
    @Scheduled(cron = "0 0 9 * * *")
    public void deleteOldAlarms() {
        try {
            log.info("[ALARM] deleteOldAlarms 시작");
            Long start = System.currentTimeMillis();

            alarmService.deleteAlarms();

            Long end = System.currentTimeMillis();
            log.info("[ALARM] deleteOldAlarms 완료 - {}ms", (end - start));
        } catch (Exception e) {
            log.error("[ALARM] deleteOldAlarms 실패 - 오래된 알림 일괄 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
