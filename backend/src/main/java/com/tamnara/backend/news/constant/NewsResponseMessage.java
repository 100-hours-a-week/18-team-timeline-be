package com.tamnara.backend.news.constant;

public final class NewsResponseMessage {
    private NewsResponseMessage() {}

    // 뉴스 성공 메시지
    public static final String HOTISSUE_NEWS_CARD_FETCH_SUCCESS = "요청하신 핫이슈 뉴스 카드 목록을 성공적으로 불러왔습니다.";
    public static final String NORMAL_NEWS_CARD_FETCH_SUCCESS = "요청하신 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.";
    public static final String NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS = "요청하신 일반 뉴스 카드 목록을 성공적으로 추가 로딩하였습니다.";
    public static final String NEWS_DETAIL_FETCH_SUCCESS = "요청하신 뉴스의 상세 정보를 성공적으로 불러왔습니다.";
    public static final String NEWS_CREATED_SUCCESS = "뉴스가 성공적으로 생성되었습니다.";
    public static final String NEWS_UPDATED_SUCCESS = "데이터가 성공적으로 업데이트되었습니다.";

    // 뉴스 예외 메시지
    public static final String NEWS_UPDATE_CONFLICT = "마지막 업데이트 이후 24시간이 지나지 않았습니다.";
    public static final String NEWS_DELETE_FORBIDDEN = "뉴스를 삭제할 권한이 없습니다.";
    public static final String NEWS_DELETE_CONFLICT = "마지막 업데이트 이후 24시간이 지나지 않았습니다.";
    public static final String CATEGORY_NOT_FOUND = "존재하지 않는 카테고리입니다.";
}
