package com.tamnara.backend.bookmark.controller;

import com.tamnara.backend.bookmark.service.BookmarkService;
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

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news/{newsId}/bookmark")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> addBookmark(@PathVariable Long newsId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 아닙니다.");
            }

            Long userId = userDetails.getUser().getId();
            Long bookmarkId = bookmarkService.addBookmark(userId, newsId);

            Map<String, Object> data = Map.of("bookmarkId", bookmarkId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "북마크가 성공적으로 추가되었습니다.",
                    "data", data
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
    public ResponseEntity<?> deleteBookmark(@PathVariable Long newsId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인된 사용자가 아닙니다.");
            }

            Long userId = userDetails.getUser().getId();
            Long bookmarkId = bookmarkService.deleteBookmark(userId, newsId);

            Map<String, Object> data = Map.of("bookmarkId", bookmarkId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "북마크가 성공적으로 해제되었습니다.",
                    "data", data
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
}
