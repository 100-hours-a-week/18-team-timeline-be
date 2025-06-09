package com.tamnara.backend.news.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NewsSchedulerServiceImpl implements NewsSchedulerService {

    private final NewsService newsService;

    public NewsSchedulerServiceImpl(NewsService newsService) {
        this.newsService = newsService;
    }

    @Override
    @Async
    @Scheduled(cron = "0 0 9 * * *")
    public void createHotissueNews() {
        try {
            log.info("[INFO] 핫이슈 뉴스 생성 시작");
            Long start = System.currentTimeMillis();

            newsService.createHotissueNews();

            Long end = System.currentTimeMillis();
            log.info("[INFO] 핫이슈 뉴스 생성 완료: {} ms", (end - start));
        } catch (Exception e) {
            log.error("[ERROR] 핫이슈 뉴스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    @Scheduled(cron = "0 0 9 * * *")
    public void deleteOldNewsAndOrphanTags() {
        try {
            log.info("[INFO] 오래된 뉴스 삭제 및 고아 태그 삭제 시작");
            Long start = System.currentTimeMillis();

            newsService.deleteOldNewsAndOrphanTags();

            Long end = System.currentTimeMillis();
            log.info("[INFO] 오래된 뉴스 삭제 및 고아 태그 삭제 완료: {}", (end - start));
        } catch (Exception e) {
            log.error("[ERROR] 오래된 뉴스 삭제 및 고아 태그 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
