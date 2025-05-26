package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentListResponse;

public interface CommentService {
    CommentListResponse getComments(Long newsId, Integer offset);
    Long save(Long userId, Long newsId, CommentCreateRequest commentCreateRequest);
    void delete(Long userId, Long newsId, Long commentId);
}
