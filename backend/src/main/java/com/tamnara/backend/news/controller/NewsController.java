package com.tamnara.backend.news.controller;

import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.news.constant.NewsResponseMessage;
import com.tamnara.backend.news.dto.NewsDetailDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.HotissueNewsListResponse;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.dto.response.category.MultiCategoryResponse;
import com.tamnara.backend.news.service.NewsService;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/hotissue")
    public ResponseEntity<WrappedDTO<HotissueNewsListResponse>> findHotissueNews() {
        try {
            HotissueNewsListResponse hotissueNewsListResponse = newsService.getHotissueNewsCardPage();

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            NewsResponseMessage.HOTISSUE_NEWS_CARD_FETCH_SUCCESS,
                            hotissueNewsListResponse
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

    @GetMapping
    public ResponseEntity<WrappedDTO<?>> findNormalNews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        try {
            if (offset == 0) {
                // 최초 요청
                Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

                MultiCategoryResponse multiCategoryResponse = newsService.getMultiCategoryPage(userId, offset);

                return ResponseEntity.ok(new WrappedDTO<>(
                        true,
                        NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_SUCCESS,
                        multiCategoryResponse
                ));
            } else {
                // 추가 요청
                Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

                Object singleCategoryResponse = newsService.getSingleCategoryPage(userId, category, offset);

                return ResponseEntity.ok().body(
                        new WrappedDTO<> (
                                true,
                                NewsResponseMessage.NORMAL_NEWS_CARD_FETCH_MORE_SUCCESS,
                                singleCategoryResponse
                        ));
            }

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> findNewsDetail(
            @PathVariable("newsId") Long newsId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

            NewsDetailDTO newsDetailDTO = newsService.getNewsDetail(newsId, userId);
            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            NewsResponseMessage.NEWS_DETAIL_FETCH_SUCCESS,
                            newsDetailResponse
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

    @PostMapping
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> createNews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody NewsCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO newsDetailDTO = newsService.save(userId, false, req);
            if (newsDetailDTO == null) {
                return ResponseEntity.noContent().build();
            }

            URI location = URI.create("/news/" + newsDetailDTO.getId());
            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);
            return ResponseEntity.created(location).body(
                    new WrappedDTO<>(
                            true,
                            NewsResponseMessage.NEWS_CREATE_SUCCESS,
                            newsDetailResponse
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

    @PatchMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> updateNews(
            @PathVariable Long newsId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO newsDetailDTO = newsService.update(newsId, userId);
            if (newsDetailDTO == null) {
                return ResponseEntity.noContent().build();
            }

            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            NewsResponseMessage.NEWS_UPDATE_SUCCESS,
                            newsDetailResponse
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

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long newsId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            if (userDetails.getUser().getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, NewsResponseMessage.NEWS_DELETE_FORBIDDEN);
            }

            newsService.delete(newsId, userDetails.getUser().getId());

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
