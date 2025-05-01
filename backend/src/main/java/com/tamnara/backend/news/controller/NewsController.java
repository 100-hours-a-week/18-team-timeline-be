package com.tamnara.backend.news.controller;

import com.tamnara.backend.common.exception.CustomException;
import com.tamnara.backend.news.dto.request.NewsCreateRequest;
import com.tamnara.backend.news.dto.response.NewsDetailResponse;
import com.tamnara.backend.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/news")
public class NewsController {
    private final NewsService newsService;

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
