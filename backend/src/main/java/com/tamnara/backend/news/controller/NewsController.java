package com.tamnara.backend.news.controller;

import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/hotissue")
    public ResponseEntity<WrappedDTO<HotissueNewsListResponse>> findHotissueNews() {
        try {
            HotissueNewsListResponse hotissueNewsListResponse = newsService.getHotissueNewsCardPage();

            return ResponseEntity.status(HttpStatus.OK).body(
                    new WrappedDTO<>(
                            true,
                            "요청하신 핫이슈 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                            hotissueNewsListResponse
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
                        "요청하신 일반 뉴스 카드 목록을 성공적으로 불러왔습니다.",
                        multiCategoryResponse
                ));
            } else {
                // 추가 요청
                Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

                Object singleCategoryResponse = newsService.getSingleCategoryPage(userId, category, offset);

                return ResponseEntity.status(HttpStatus.OK).body(
                        new WrappedDTO<> (
                                true,
                                "요청하신 일반 뉴스 카드 목록을 성공적으로 추가 로딩하였습니다.",
                                singleCategoryResponse
                        ));
            }
        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> findNewsDetail(@PathVariable("newsId") Long newsId,
                                                                         @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Long userId = (userDetails != null && userDetails.getUser() != null) ? userDetails.getUser().getId() : null;

            NewsDetailDTO newsDetailDTO = newsService.getNewsDetail(newsId, userId);
            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            new WrappedDTO<>(
                                    true,
                                    "요청하신 뉴스의 상세 정보를 성공적으로 불러왔습니다.",
                                    newsDetailResponse
                            )
                    );
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
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> createNews(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                             @RequestBody NewsCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO newsDetailDTO = newsService.save(userId, false, req);
            if (newsDetailDTO == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new WrappedDTO<>(
                            true,
                            "뉴스가 성공적으로 생성되었습니다.",
                            newsDetailResponse
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

    @PatchMapping("/{newsId}")
    public ResponseEntity<WrappedDTO<NewsDetailResponse>> updateNews(@PathVariable Long newsId,
                                                                             @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();

            NewsDetailDTO newsDetailDTO = newsService.update(newsId, userId);
            if (newsDetailDTO == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            NewsDetailResponse newsDetailResponse = new NewsDetailResponse(newsDetailDTO);

            return ResponseEntity.status(HttpStatus.OK).body(
                    new WrappedDTO<>(
                            true,
                            "데이터가 성공적으로 업데이트되었습니다.",
                            newsDetailResponse
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

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long newsId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            if (userDetails.getUser().getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "뉴스를 삭제할 권한이 없습니다.");
            }

            newsService.delete(newsId, userDetails.getUser().getId());

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
