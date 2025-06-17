package com.tamnara.backend.global.constant;

public class ResponseMessage {
    // 공통 예외 메시지
    public static final String BAD_REQUEST = "요청 형식이 올바르지 않습니다.";
    public static final String INTERNAL_SERVER_ERROR = "서버 내부에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.";

    // 회원 예외 메시지
    public static final String USER_NOT_FOUND = "존재하지 않는 회원입니다.";
    public static final String USER_NOT_CERTIFICATION = "인증되지 않은 사용자입니다.";
    public static final String USER_UNAUTHORIZED = "해당 요청에 대한 접근 권한이 없습니다.";
    public static final String USER_FORBIDDEN = "유효하지 않은 계정입니다.";
    public static final String INVALID_TOKEN = "유효하지 않은 토큰입니다.";

    // 뉴스 예외 메시지
    public static final String NEWS_NOT_FOUND = "존재하지 않는 뉴스입니다.";
}
