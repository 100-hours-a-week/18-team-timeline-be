package com.tamnara.backend.news.service;

public interface NewsSchedulerService {
    void createHotissueNews();
    void deleteOldNewsAndOrphanTags();
}
