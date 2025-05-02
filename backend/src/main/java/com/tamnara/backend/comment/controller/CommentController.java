package com.tamnara.backend.comment.controller;

import com.tamnara.backend.comment.dto.CommentCreateRequest;
import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.service.CommentService;
import com.tamnara.backend.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/news/{newsId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final Integer PAGE_SIZE = 20;

    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") Integer offset) {

        try {
            int page = offset / PAGE_SIZE;
            int nextOffset = (page + 1) * PAGE_SIZE;

            List<CommentDTO> comments = commentService.getComments(newsId, page, PAGE_SIZE);
            boolean hasNext = !commentService.getComments(newsId, nextOffset, PAGE_SIZE).isEmpty();

            Map<String, Object> data = Map.of(
                    "comments", comments,
                    "offset", nextOffset,
                    "hasNext", hasNext
            );

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", data
            ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long newsId, @RequestBody CommentCreateRequest req) {
        try {
            Long commentId = commentService.save(newsId, req);

            Map<String, Object> data = Map.of(
                    "commendId", commentId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", data
            ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long newsId, @RequestParam Long commentId) {
        try {
            Long deletedCommentId = commentService.delete(newsId, commentId);

            Map<String, Object> data = Map.of(
                    "commendId", commentId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", data
            ));
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
