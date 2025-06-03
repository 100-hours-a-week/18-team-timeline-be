package com.tamnara.backend.bookmark.controller;

import com.tamnara.backend.bookmark.constant.BookmarkResponseMessage;
import com.tamnara.backend.bookmark.dto.response.BookmarkListResponse;
import com.tamnara.backend.bookmark.service.BookmarkService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/bookmarks")
public class BookmarkListController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<WrappedDTO<BookmarkListResponse>> findBookmarkedNewsList(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();

            BookmarkListResponse bookmarkListResponse = bookmarkService.getBookmarkedNewsList(userId, offset);

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            BookmarkResponseMessage.BOOKMARKED_NEWS_LIST_FETCH_SUCCESS,
                            bookmarkListResponse
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
}
