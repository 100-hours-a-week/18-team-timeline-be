package com.tamnara.backend.news.controller;

import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    private final Integer PAGE_SIZE = 20;

    @GetMapping("/hotissue")
    public ResponseEntity<?> findHotissueNews() {
        try {
            List<NewsCardDTO> newsCards = newsService.getHotissueNewsCardPage(null);

            Map<String, Object> newsList = Map.of("newsList", newsCards);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", newsList
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
    public ResponseEntity<?> findNormalNews(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            Long userId = null;
            if (!(userDetails == null || userDetails.getUser() == null)) {
                userId = userDetails.getUser().getId();
            }

            if (category != null) {
                // 추가 로딩
                if (offset < 0) {
                    throw new IllegalArgumentException();
                }

                int pageNum = offset / PAGE_SIZE;
                int nextOffset = (pageNum + 1) * PAGE_SIZE;

                List<NewsCardDTO> newsCards = newsService.getNormalNewsCardPage(userId, category, pageNum, PAGE_SIZE);

                boolean hasNext = !newsService.getNormalNewsCardPage(userId, category, pageNum + 1, PAGE_SIZE).isEmpty();

                Map<String, Object> data = Map.of(
                        category, newsCards,
                        "offset", nextOffset,
                        "hasNext", hasNext
                );

                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "success", true,
                        "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                        "data", data
                ));
            } else {
                // 최초 요청
                Map<String, List<NewsCardDTO>> newsCardsMap = newsService.getNormalNewsCardPages(userId, 0, PAGE_SIZE);
                Map<String, Object> data = new HashMap<>();

                for (Map.Entry<String, List<NewsCardDTO>> entry : newsCardsMap.entrySet()) {
                    String categoryName = entry.getKey();
                    List<NewsCardDTO> newsList = entry.getValue();

                    boolean hasNext;
                    if (Objects.equals(categoryName, "ALL")) {
                        hasNext = newsService.getNormalNewsCardPage(null,1, PAGE_SIZE).isEmpty();
                    } else {
                        hasNext = !newsService.getNormalNewsCardPage(null, categoryName, 1, PAGE_SIZE).isEmpty();
                    }

                    Map<String, Object> categoryNewsData = new HashMap<>();
                    categoryNewsData.put("newsList", newsList);
                    categoryNewsData.put("offset", PAGE_SIZE);
                    categoryNewsData.put("hasNext", hasNext);

                    data.put(categoryName, categoryNewsData);
                }

                return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                        "success", true,
                        "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                        "data", data
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
    public ResponseEntity<?> findNewsDetail(@PathVariable("newsId") Long newsId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            Long userId = null;
            if (!(userDetails == null || userDetails.getUser() == null)) {
                userId = userDetails.getUser().getId();
            }

            NewsDetailResponse res = newsService.getNewsDetail(newsId, userId);

            Map<String, Object> data = Map.of("news", res);
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
    public ResponseEntity<?> createNews(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody NewsCreateRequest req
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();
            NewsDetailResponse res = newsService.save(userId, false, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "데이터가 성공적으로 생성되었습니다.",
                    "data", res
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
    public ResponseEntity<?> updateNews(@PathVariable Long newsId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            Long userId = userDetails.getUser().getId();
            NewsDetailResponse res = newsService.update(newsId, userId);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "데이터가 성공적으로 업데이트되었습니다.",
                    "data", res
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
    public ResponseEntity<?> deleteNews(@PathVariable Long newsId,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 회원입니다.");
            }

            if (userDetails.getUser().getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "뉴스를 삭제할 권한이 없습니다.");
            }

            Long resNewsId = newsService.delete(newsId, null);

            Map<String, Object> data = Map.of("newsId", resNewsId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                    "success", true,
                    "message", "데이터가 성공적으로 삭제되었습니다.",
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
