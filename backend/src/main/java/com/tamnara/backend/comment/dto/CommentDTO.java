package com.tamnara.backend.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentDTO {
    Long id;
    Long userId;
    String content;
    LocalDateTime createdAt;
}
