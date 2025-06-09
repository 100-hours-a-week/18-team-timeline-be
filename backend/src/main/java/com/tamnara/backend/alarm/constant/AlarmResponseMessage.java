package com.tamnara.backend.alarm.constant;

public final class AlarmResponseMessage {
    private AlarmResponseMessage() {}

    // 알림 성공 메시지
    public static final String ALARM_FETCH_SUCCESS = "요청하신 알림 목록을 성공적으로 불러왔습니다.";
    public static final String ALARM_CHECK_SUCCESS = "알림을 성공적으로 확인하였습니다.";

    // 알림 예외 메시지
    public static final String ALARM_NOT_FOUND = "존재하지 않는 알림입니다.";
}
