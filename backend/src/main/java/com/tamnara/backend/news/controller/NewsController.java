package com.tamnara.backend.news.controller;

import com.tamnara.backend.common.exception.CustomException;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {

    private final NewsService newsService;

    private final Integer PAGE_SIZE = 20;

    @GetMapping("/hotissue")
    public ResponseEntity<?> findHotissueNews() {
        try {
            List<NewsCardDTO> newsCards = newsService.getNewsCardPage(null, true, 0, 3);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", newsCards
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

    @GetMapping
    public ResponseEntity<?> findNormalNews(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            int pageNum = offset / PAGE_SIZE;
            int nextOffset = (pageNum + 1) * PAGE_SIZE;

            if (category != null) {
                // 추가 로딩
                if (offset <= 0) {
                    throw new IllegalArgumentException("추가 요청일 경우 offset은 0이어야 합니다.");
                }

                List<NewsCardDTO> newsCards = newsService.getNewsCardPage(null, false, category, pageNum, PAGE_SIZE);

                boolean hasNext = !newsService.getNewsCardPage(null, false, category, pageNum + 1, PAGE_SIZE).isEmpty();

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
                if (offset > 0) {
                    throw new IllegalArgumentException("최초 요청일 경우 offset은 0보다 큰 값이어야 합니다.");
                }

                Map<String, List<NewsCardDTO>> newsCardsMap = newsService.getNormalNewsCardPages(null, false, pageNum, PAGE_SIZE);
                Map<String, Object> data = new HashMap<>();

                for (Map.Entry<String, List<NewsCardDTO>> entry : newsCardsMap.entrySet()) {
                    String categoryName = entry.getKey();
                    List<NewsCardDTO> newsList = entry.getValue();

                    boolean hasNext;
                    if (categoryName == "ALL") {
                        hasNext = newsService.getNewsCardPage(null, false,pageNum + 1, PAGE_SIZE).isEmpty();
                    } else {
                        hasNext = !newsService.getNewsCardPage(null, false, categoryName, pageNum + 1, PAGE_SIZE).isEmpty();
                    }

                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("newsList", newsList);
                    categoryData.put("offset", nextOffset);
                    categoryData.put("hasNext", hasNext);

                    data.put(categoryName, categoryData);
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
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<?> findNewsDetail(@PathVariable("newsId") Long newsId) {
        try {
            NewsDetailResponse res = newsService.getNewsDetail(newsId, null);

            Map<String, Object> data = Map.of(
                    "news", res,
                    "comment", ""
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
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }

    @PostMapping
    public ResponseEntity<?> createNews(@ModelAttribute NewsCreateRequest req) {
        try {
            NewsDetailResponse res = newsService.save(null, false, req);
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
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }

    @PatchMapping("/{newsId}")
    public ResponseEntity<?> updateNews(@PathVariable Long newsId) {
        try {
            NewsDetailResponse res = newsService.update(newsId, null);
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
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<?> deleteNews(@PathVariable Long newsId) {
        try {
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
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");
        }
    }
}
