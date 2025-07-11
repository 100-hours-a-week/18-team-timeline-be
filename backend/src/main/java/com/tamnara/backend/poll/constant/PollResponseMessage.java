package com.tamnara.backend.poll.constant;

public final class PollResponseMessage {
    private PollResponseMessage() {}

    public static final String POLL_CREATED = "투표가 성공적으로 생성되었습니다.";
    public static final String POLL_SCHEDULED = "투표가 성공적으로 SCHEDULED 처리되었습니다.";
    public static final String POLL_OK = "요청하신 정보를 성공적으로 불러왔습니다.";
    public static final String VOTE_SUCCESS = "투표가 완료되었습니다.";
    public static final String VOTE_STATISTICS = "투표 결과 통계를 성공적으로 불러왔습니다.";

    public static final String POLL_NOT_PUBLISHED = "투표를 공개할 수 없습니다.";
    public static final String POLL_SCHEDULED_CONFLICT = "대기 상태의 투표가 아니라 공개 예정 상태로 전환할 수 없습니다.";
    public static final String POLL_NOT_FOUND = "요청하신 투표를 찾을 수 없습니다.";
    public static final String MIN_CHOICES_EXCEED_MAX = "최소 선택 수는 최대 선택 수보다 클 수 없습니다.";
    public static final String POLL_NOT_IN_VOTING_PERIOD = "투표 기간이 아닙니다.";
    public static final String POLL_OR_OPTION_NOT_FOUND = "요청하신 투표 또는 선택지를 찾을 수 없습니다.";
    public static final String POLL_INVALID_SELECTION_COUNT = "응답 수가 올바르지 않습니다.";
    public static final String POLL_ALREADY_VOTED = "이미 투표하셨습니다.";
}
