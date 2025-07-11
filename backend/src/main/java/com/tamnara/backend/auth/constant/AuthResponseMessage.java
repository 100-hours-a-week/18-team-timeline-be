package com.tamnara.backend.auth.constant;

public final class AuthResponseMessage {
    private AuthResponseMessage() {}

    public static final String KAKAO_LOGIN_URL_GENERATGED = "카카오 로그인 요청 URL이 성공적으로 생성되었습니다.";
    public static final String KAKAO_LOGIN_SUCCESSFUL = "카카오 로그인이 성공적으로 완료되었습니다.";

    public static final String KAKAO_BAD_GATEWAY = "카카오 서버 요청에 실패하였습니다.";
    public static final String PARSING_ACCESS_TOKEN_FAILS = "액세스 토큰 파싱에 실패했습니다.";
    public static final String PARSING_USER_INFO_FAILS = "액세스 토큰 파싱에 실패했습니다.";
    public static final String EXTERNAL_API_TIMEOUT = "외부 API 호출에 시간이 너무 오래 걸립니다.";
    public static final String EXTERNAL_API_CALL_FAIL = "외부 API 호출에 실패했습니다.";

    public static final String NOT_LOGGED_IN = "로그인이 필요합니다.";
    public static final String IS_LOGGED_IN = "로그인 상태입니다.";
    public static final String REFRESH_TOKEN_SUCCESS = "리프레시 토큰을 성공적으로 갱신했습니다.";
    public static final String REFRESH_TOKEN_INVALID = "리프레시 토큰이 유효하지 않습니다.";
}
