package com.tamnara.backend.poll.constant;

public class PollResponseMessage {
    public static final String POLL_CREATED = "투표가 성공적으로 생성되었습니다.";
    public static final String POLL_OK = "요청하신 정보를 성공적으로 불러왔습니다.";

    public static final String POLL_FORBIDDEN = "투표를 공개할 수 없습니다.";
    public static final String POLL_NOT_FOUND = "요청하신 투표를 찾을 수 없습니다.";

    public static final String MIN_CHOICES_EXCEED_MAX = "최소 선택 수는 최대 선택 수보다 클 수 없습니다.";
    public static final String START_DATE_LATER_THAN_END_DATE = "시작일은 종료일보다 앞서야 합니다.";
}
