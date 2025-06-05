package com.tamnara.backend.news.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.QNews;
import com.tamnara.backend.news.domain.QNewsTag;
import com.tamnara.backend.news.domain.QTag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class NewsSearchRepositoryImpl implements NewsSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<News> searchNewsPageByTags(List<String> keywords, Pageable pageable) {
        QNews news = QNews.news;
        QTag tag = QTag.tag;
        QNewsTag newsTag = QNewsTag.newsTag;

        List<News> content = queryFactory
                .select(news)
                .from(newsTag)
                .join(newsTag.news, news)
                .join(newsTag.tag, tag)
                .where(tag.name.in(keywords))
                .groupBy(news.id)
                .orderBy(
                        tag.name.count().desc(),
                        news.updatedAt.desc(),
                        news.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(news.countDistinct())
                .from(newsTag)
                .join(newsTag.news, news)
                .join(newsTag.tag, tag)
                .where(tag.name.in(keywords))
                .fetchOne();

        return PageableExecutionUtils.getPage(content, pageable, () -> total != null ? total : 0L);
    }
}
