package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NewsTest {

    @Test
    void 뉴스_기본값_초기화_검증() {
        // given & when
        News news = new News();

        // then
        assertThat(news.getIsHotissue()).isFalse();
        assertThat(news.getIsPublic()).isTrue();
        assertThat(news.getViewCount()).isEqualTo(1L);
        assertThat(news.getUpdateCount()).isEqualTo(1L);
        assertThat(news.getRatioPosi()).isZero();
        assertThat(news.getRatioNeut()).isZero();
        assertThat(news.getRatioNega()).isZero();
    }

    @Test
    void 뉴스_제목과_미리보기_내용과_핫이슈_여부와_공개_여부_설정_검증() {
        // given
        News news = new News();

        // when
        news.setTitle("테스트 제목");
        news.setSummary("테스트 미리보기 내용");
        news.setIsHotissue(true);
        news.setIsPublic(false);

        // then
        assertThat(news.getTitle()).isEqualTo("테스트 제목");
        assertThat(news.getSummary()).isEqualTo("테스트 미리보기 내용");
        assertThat(news.getIsHotissue()).isTrue();
        assertThat(news.getIsPublic()).isFalse();
    }

    @Test
    void 뉴스_조회수_증가_로직_검증() {
        // given
        News news = new News();
        news.setViewCount(5L);

        // when
        news.setViewCount(news.getViewCount() + 1);

        // then
        assertThat(news.getViewCount()).isEqualTo(6L);
    }

    @Test
    void 뉴스_업데이트_횟수_증가_로직_검증() {
        // given
        News news = new News();
        news.setUpdateCount(5L);

        // when
        news.setUpdateCount(news.getUpdateCount() + 1);

        // then
        assertThat(news.getUpdateCount()).isEqualTo(6L);
    }

    @Test
    void ID가_동일하면_동일한_뉴스_검증() {
        // given
        News news1 = new News();
        News news2 = new News();

        // when
        news1.setId(1L);
        news2.setId(1L);

        // then
        assertThat(news1).isEqualTo(news2);
    }
}
