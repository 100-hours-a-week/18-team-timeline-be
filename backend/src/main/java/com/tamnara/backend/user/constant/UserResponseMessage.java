package com.tamnara.backend.user.constant;

public final class UserResponseMessage {
    private UserResponseMessage() {}

    public static final String ID_UNAVAILABLE = "잘못된 사용자 ID 형식입니다.";

    public static final String EMAIL_BAD_REQUEST = "이메일 형식이 올바르지 않습니다.";
    public static final String EMAIL_AVAILABLE = "사용 가능한 이메일입니다.";
    public static final String EMAIL_UNAVAILABLE = "이미 사용 중인 이메일입니다.";

    public static final String NICKNAME_UNAVAILABLE = "이미 사용 중인 닉네임입니다.";

    public static final String USER_INFO_RETRIEVED = "요청하신 회원 정보를 성공적으로 불러왔습니다.";
    public static final String USER_INFO_MODIFIED = "회원 정보가 성공적으로 수정되었습니다.";

    public static final String LOGOUT_SUCCESSFUL = "정상적으로 로그아웃 되었습니다.";

    public static final String WITHDRAWAL_SUCCESSFUL ="회원 탈퇴 처리가 완료되었습니다.";

    public static final String REGISTER_SUCCESSFUL = "회원가입이 성공적으로 완료되었습니다.";
}
