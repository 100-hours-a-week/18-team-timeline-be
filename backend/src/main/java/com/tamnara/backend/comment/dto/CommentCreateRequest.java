package com.tamnara.backend.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
@AllArgsConstructor
public class CommentCreateRequest {
    @Length(min = 1, message = "댓글란은 비어 있을 수 없습니다.")
    @Length(max = 150, message = "댓글 최대 길이를 초과하였습니다.")
    String content;
}
