package com.tamnara.backend.auth.constant;

public class AuthResponseMessage {
    public static final String KAKAO_LOGIN_URL_GENERATGED = "카카오 로그인 요청 URL이 성공적으로 생성되었습니다. ";
    public static final String KAKAO_LOGIN_SUCCESSFUL = "카카오 로그인이 성공적으로 완료되었습니다. ";

    public static final String KAKAO_BAD_GATEWAY = "❌ 카카오 서버 요청 실패: ";
    public static final String PARSING_ACCESS_TOKEN_FAILS = "액세스 토큰 파싱에 실패했습니다. ";
    public static final String PARSING_USER_INFO_FAILS = "액세스 토큰 파싱에 실패했습니다. ";
    public static final String EXTERNAL_API_TIMEOUT = "API 호출에 시간이 너무 오래 걸립니다. ";
}
