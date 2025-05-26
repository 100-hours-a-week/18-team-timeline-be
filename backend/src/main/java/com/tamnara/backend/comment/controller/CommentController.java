package com.tamnara.backend.comment.controller;

import com.tamnara.backend.comment.dto.CommentDTO;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentCreateResponse;
import com.tamnara.backend.comment.dto.response.CommentListResponse;
import com.tamnara.backend.comment.service.CommentService;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/{newsId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final Integer PAGE_SIZE = 20;

    @GetMapping
    public ResponseEntity<WrappedDTO<CommentListResponse>> getComments(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") Integer offset) {

        try {
            int page = offset / PAGE_SIZE;
            int nextOffset = (page + 1) * PAGE_SIZE;

            List<CommentDTO> comments = commentService.getComments(newsId, page, PAGE_SIZE);
            boolean hasNext = !commentService.getComments(newsId, nextOffset, PAGE_SIZE).isEmpty();

            CommentListResponse commentListResponse = new CommentListResponse(
                    comments,
                    nextOffset,
                    hasNext
            );

            return ResponseEntity.ok().body(
                    new WrappedDTO<> (
                        true,
                        "요청하신 댓글 목록을 성공적으로 불러왔습니다.",
                        commentListResponse
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
    public ResponseEntity<WrappedDTO<CommentCreateResponse>> createComment(@PathVariable Long newsId,
                                                                           @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                           @RequestBody CommentCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUsername() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
            }

            Long userId = userDetails.getUser().getId();
            Long commentId = commentService.save(userId, newsId, req);

            CommentCreateResponse commentCreateResponse = new CommentCreateResponse(commentId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<> (
                        true,
                        "댓글이 성공적으로 생성되었습니다.",
                        commentCreateResponse
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
    public ResponseEntity<Void> deleteComment(@PathVariable Long newsId,
                                           @AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestParam Long commentId
    ) {
        try {
            if (userDetails == null || userDetails.getUsername() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
            }

            Long userId = userDetails.getUser().getId();
            commentService.delete(userId, newsId, commentId);

            return ResponseEntity.noContent().build();

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
