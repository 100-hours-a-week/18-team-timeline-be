package com.tamnara.backend.alarm.dto.response;

import com.tamnara.backend.alarm.dto.AlarmCardDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AlarmListResponse {
    private String type;
    private List<AlarmCardDTO> alarms;
}
