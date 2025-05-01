package com.tamnara.backend.news.controller;

import com.tamnara.backend.common.exception.CustomException;
import com.tamnara.backend.news.dto.NewsCardDTO;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            List<NewsCardDTO> newsCards = newsService.getNewsPage(null, true, 0, 3);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", newsCards
            ));
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> findNormalNews(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer offset) {
        try {
            int pageNum = offset / PAGE_SIZE;
            int nextOffset = (pageNum + 1) * PAGE_SIZE;

            if (offset > 0) {
                // 추가 로딩
                List<NewsCardDTO> newsCards = newsService.getNewsPage(null, false, category, pageNum, PAGE_SIZE);

                boolean hasNext = !newsService.getNewsPage(null, false, category, pageNum + 1, PAGE_SIZE).isEmpty();

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
                Map<String, List<NewsCardDTO>> newsCardsMap = newsService.getNormalNewsPages(null, false, category, pageNum, PAGE_SIZE);
                Map<String, Object> data = new HashMap<>();

                for (Map.Entry<String, List<NewsCardDTO>> entry : newsCardsMap.entrySet()) {
                    String categoryName = entry.getKey();
                    List<NewsCardDTO> newsList = entry.getValue();

                    boolean hasNext = !newsService.getNewsPage(null, false, categoryName, pageNum + 1, PAGE_SIZE).isEmpty();

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
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<?> findNewsDetail(@PathVariable("newsId") Long newsId) {
        try {
            NewsDetailResponse res = newsService.getNewsDetail(newsId, null);

            Map<String, Object> data = Map.of(
                    "news", res,
                    "comment", null
            );

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "요청하신 데이터를 성공적으로 불러왔습니다.",
                    "data", data
            ));
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createNews(@ModelAttribute NewsCreateRequest req) {
        try {
            NewsDetailResponse res = newsService.save(null, false, req);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "success", true,
                    "message", "데이터가 성공적으로 생성되었습니다.",
                    "data", res
            ));
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
