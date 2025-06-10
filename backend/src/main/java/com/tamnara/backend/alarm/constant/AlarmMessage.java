package com.tamnara.backend.alarm.constant;

public final class AlarmMessage {
    private AlarmMessage() {}

    // 전체 알림 메시지
    public static final String HOTISSUE_CREATE_TITLE = "오늘의 핫이슈 생성";
    public static final String HOTISSUE_CREATE_CONTENT = "오늘의 핫이슈가 새로 업데이트되었습니다. 배너를 확인해 주세요.";

    // 북마크 알림 메시지
    public static final String BOOKMARK_UPDATE_TITLE = "북마크 업데이트";
    public static final String BOOKMARK_UPDATE_CONTENT = "'%s'이/가 업데이트되었습니다.";

    public static final String BOOKMARK_DELETE_WARNING_TITLE = "북마크 삭제 예정";
    public static final String BOOKMARK_DELETE_WARNING_CONTENT = "'%s'이/가 24시간 뒤 삭제될 예정입니다.";

    public static final String BOOKMARK_DELETION_TITLE = "북마크 삭제";
    public static final String BOOKMARK_DELETION_CONTENT = "'%s'가 삭제되었습니다.";

    // 투표 알림 메시지
    public static final String POLL_START_TITLE = "이번 주 투표 시작";
    public static final String POLL_START_CONTENT = "이번 주 투표 '%s'에 참여하세요!";

    public static final String POLL_RESULT_TITLE = "지난 투표 결과 공개";
    public static final String POLL_RESULT_CONTENT = "'%s'에 대한 투표 결과를 확인해 보세요.";
}
