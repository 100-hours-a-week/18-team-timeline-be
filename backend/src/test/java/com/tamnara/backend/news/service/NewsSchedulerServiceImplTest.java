package com.tamnara.backend.news.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NewsSchedulerServiceImplTest {

    @Mock private NewsService newsService;

    @InjectMocks private NewsSchedulerServiceImpl newsSchedulerService;

    @Test
    void 핫이슈_뉴스_생성_검증() {
        // when
        newsSchedulerService.createHotissueNews();

        // then
        verify(newsService, times(1)).createHotissueNews();
    }

    @Test
    void 오래된_뉴스_및_고아_태그_삭제_검증() {
        // when
        newsSchedulerService.deleteOldNewsAndOrphanTags();

        // then
        verify(newsService, times(1)).deleteOldNewsAndOrphanTags();
    }
}
