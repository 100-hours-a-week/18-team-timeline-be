package com.tamnara.backend.alarm.service;

import com.tamnara.backend.alarm.dto.response.AlarmListResponse;

public interface AlarmService {
    AlarmListResponse getAllAlarmPageByUserId(Long userId);
    AlarmListResponse getBookmarkAlarmPageByUserId(Long userId);
    Long checkUserAlarm(Long userAlarmId, Long userId);

    void deleteAlarms();
}
