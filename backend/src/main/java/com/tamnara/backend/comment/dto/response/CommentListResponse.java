package com.tamnara.backend.comment.dto.response;

import com.tamnara.backend.comment.dto.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponse {
    private List<CommentDTO> comments;
    private int offset;
    private boolean hasNext;
}
