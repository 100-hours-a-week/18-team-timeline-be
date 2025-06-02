package com.tamnara.backend.bookmark.controller;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.service.BookmarkService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/{newsId}/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<WrappedDTO<BookmarkAddResponse>> addBookmark(@PathVariable Long newsId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();

            BookmarkAddResponse bookmarkAddResponse = bookmarkService.save(userId, newsId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            BookmarkResponseMessage.BOOKMARK_ADDED_SUCCESS,
                            bookmarkAddResponse
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

    @DeleteMapping
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long newsId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();
            bookmarkService.delete(userId, newsId);

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
