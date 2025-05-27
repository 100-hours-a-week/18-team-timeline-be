package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NewsImageTest {

    @Test
    void 뉴스_이미지_초기화_검증() {
        // given & when
        NewsImage newsImage = new NewsImage();

        // then
        assertThat(newsImage.getNews()).isNull();
        assertThat(newsImage.getUrl()).isNull();
    }

    @Test
    void 뉴스_이미지_연관관계와_URL_설정_검증() {
        // given
        News news = new News();
        NewsImage newsImage = new NewsImage();

        // when
        newsImage.setNews(news);
        newsImage.setUrl("url");

        // then
        assertThat(newsImage.getNews()).isEqualTo(news);
        assertThat(newsImage.getUrl()).isEqualTo("url");
    }

    @Test
    void ID가_동일하면_동일한_이미지_검증() {
        // given
        NewsImage newsImage1 = new NewsImage();
        NewsImage newsImage2 = new NewsImage();

        // when
        newsImage1.setId(1L);
        newsImage2.setId(1L);

        // then
        assertThat(newsImage1).isEqualTo(newsImage2);
    }
}
