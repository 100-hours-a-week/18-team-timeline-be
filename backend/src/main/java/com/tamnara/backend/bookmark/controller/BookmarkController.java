package com.tamnara.backend.bookmark.controller;

import com.tamnara.backend.bookmark.dto.response.BookmarkAddResponse;
import com.tamnara.backend.bookmark.service.BookmarkService;
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
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 아닙니다.");
            }

            Long userId = userDetails.getUser().getId();
            Long bookmarkId = bookmarkService.addBookmark(userId, newsId);

            BookmarkAddResponse data = new BookmarkAddResponse(bookmarkId);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            "북마크가 성공적으로 추가되었습니다.",
                            data
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBookmark(@PathVariable Long newsId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 아닙니다.");
            }

            Long userId = userDetails.getUser().getId();
            bookmarkService.deleteBookmark(userId, newsId);

            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }
}
