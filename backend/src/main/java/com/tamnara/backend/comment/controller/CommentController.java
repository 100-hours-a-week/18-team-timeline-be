package com.tamnara.backend.comment.controller;

import com.tamnara.backend.comment.constant.CommentResponseMessage;
import com.tamnara.backend.comment.dto.request.CommentCreateRequest;
import com.tamnara.backend.comment.dto.response.CommentCreateResponse;
import com.tamnara.backend.comment.dto.response.CommentListResponse;
import com.tamnara.backend.comment.service.CommentService;
import com.tamnara.backend.global.constant.ResponseMessage;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/{newsId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<WrappedDTO<CommentListResponse>> getComments(
            @PathVariable Long newsId,
            @RequestParam(defaultValue = "0") Integer offset
    ) {

        try {
            CommentListResponse commentListResponse = commentService.getComments(newsId, offset);

            return ResponseEntity.ok().body(
                    new WrappedDTO<> (
                        true,
                        CommentResponseMessage.COMMENT_LIST_FETCH_SUCCESS,
                        commentListResponse
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<WrappedDTO<CommentCreateResponse>> createComment(
            @PathVariable Long newsId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody CommentCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUsername() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();
            Long commentId = commentService.save(userId, newsId, req);

            CommentCreateResponse commentCreateResponse = new CommentCreateResponse(commentId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<> (
                            true,
                            CommentResponseMessage.COMMENT_CREATED_SUCCESS,
                            commentCreateResponse
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long newsId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long commentId
    ) {
        try {
            if (userDetails == null || userDetails.getUsername() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();
            commentService.delete(userId, newsId, commentId);

            return ResponseEntity.noContent().build();

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
