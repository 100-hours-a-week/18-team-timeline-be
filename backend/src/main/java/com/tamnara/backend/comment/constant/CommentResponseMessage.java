package com.tamnara.backend.comment.constant;

public final class CommentResponseMessage {
    private CommentResponseMessage() {}

    // 댓글 성공 메시지
    public static final String COMMENT_LIST_FETCH_SUCCESS = "요청하신 댓글 목록을 성공적으로 불러왔습니다.";
    public static final String COMMENT_CREATED_SUCCESS = "댓글이 성공적으로 생성되었습니다.";

    // 댓글 예외 메시지
    public static final String COMMENT_NOT_FOUND = "존재하지 않는 댓글입니다.";
    public static final String COMMENT_DELETE_FORBIDDEN = "댓글을 삭제할 권한이 없습니다.";
}
