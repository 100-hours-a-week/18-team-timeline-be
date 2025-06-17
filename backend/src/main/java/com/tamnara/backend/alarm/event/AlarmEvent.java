package com.tamnara.backend.alarm.event;

import com.tamnara.backend.alarm.domain.AlarmType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class AlarmEvent {
    private final List<Long> receiverId;
    private final String title;
    private final String content;
    private final AlarmType targetType;
    private final Long targetId;
}
