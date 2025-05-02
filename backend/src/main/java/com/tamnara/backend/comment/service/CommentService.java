package com.tamnara.backend.comment.service;

import com.tamnara.backend.comment.dto.CommentCreateRequest;
import com.tamnara.backend.comment.dto.CommentDTO;

import java.util.List;

public interface CommentService {
    List<CommentDTO> getComments(Long newsId, Integer page, Integer size);
    Long save(Long newsId, CommentCreateRequest commentCreateRequest);
    Long delete(Long newsId, Long commentId);
}
