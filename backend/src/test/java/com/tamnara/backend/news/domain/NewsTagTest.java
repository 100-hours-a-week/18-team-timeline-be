package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NewsTagTest {

    @Test
    void 뉴스태그_초기화_검증() {
        // given & when
        NewsTag newsTag = new NewsTag();

        // then
        assertThat(newsTag.getNews()).isNull();
        assertThat(newsTag.getTag()).isNull();
    }

    @Test
    void 뉴스태그_뉴스와_태그_설정_검증() {
        // given
        News news = new News();
        Tag tag = new Tag();
        NewsTag newsTag = new NewsTag();

        // when
        newsTag.setNews(news);
        newsTag.setTag(tag);

        // then
        assertThat(newsTag.getNews()).isEqualTo(news);
        assertThat(newsTag.getTag()).isEqualTo(tag);
    }

    @Test
    void ID가_동일하면_동일한_뉴스태그() {
        // given
        NewsTag newsTag1 = new NewsTag();
        NewsTag newsTag2 = new NewsTag();

        // when
        newsTag1.setId(1L);
        newsTag2.setId(1L);

        // then
        assertThat(newsTag1).isEqualTo(newsTag2);
    }
}
