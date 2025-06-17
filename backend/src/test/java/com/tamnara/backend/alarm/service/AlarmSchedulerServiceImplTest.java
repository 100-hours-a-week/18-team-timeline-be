package com.tamnara.backend.alarm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AlarmSchedulerServiceImplTest {

    @Mock private AlarmService alarmService;
    @InjectMocks private AlarmSchedulerServiceImpl alarmSchedulerService;

    @Test
    void 오래된_알림_일괄_삭제_검증() {
        // when
        alarmSchedulerService.deleteOldAlarms();

        // then
        verify(alarmService, times(1)).deleteAlarms();
    }
}
