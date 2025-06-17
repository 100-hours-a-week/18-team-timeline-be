package com.tamnara.backend.alarm.config;

import com.tamnara.backend.alarm.service.AlarmService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AlarmServiceMockConfig {
    @Bean
    public AlarmService alarmService() {
        return Mockito.mock(AlarmService.class);
    }
}
