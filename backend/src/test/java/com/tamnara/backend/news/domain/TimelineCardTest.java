package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TimelineCardTest {

    @Test
    void 타임라인_카드_초기화_검증() {
        // given & when
        TimelineCard timelineCard = new TimelineCard();

        // then
        assertThat(timelineCard.getNews()).isNull();
        assertThat(timelineCard.getTitle()).isNull();
        assertThat(timelineCard.getContent()).isNull();
        assertThat(timelineCard.getSource()).isNull();
        assertThat(timelineCard.getDuration()).isEqualTo(TimelineCardType.DAY);
    }

    @Test
    void 타임라인_카드에_제목과_내용과_출처와_종류_설정_검증() {
        // given
        TimelineCard timelineCard = new TimelineCard();
        News news = new News();
        List<String> source = new ArrayList<>();
        source.add("출처_url_1");
        source.add("출처_url_2");

        // when
        timelineCard.setNews(news);
        timelineCard.setTitle("테스트 제목");
        timelineCard.setContent("테스트 내용");
        timelineCard.setSource(source);
        timelineCard.setDuration(TimelineCardType.WEEK);

        // then
        assertThat(timelineCard.getNews()).isEqualTo(news);
        assertThat(timelineCard.getTitle()).isEqualTo("테스트 제목");
        assertThat(timelineCard.getContent()).isEqualTo("테스트 내용");
        assertThat(timelineCard.getSource()).isEqualTo(source);
        assertThat(timelineCard.getDuration()).isEqualTo(TimelineCardType.WEEK);
    }

    @Test
    void ID가_동일하면_동일한_타임라인_카드_검증() {
        // given
        TimelineCard timelineCard1 = new TimelineCard();
        TimelineCard timelineCard2 = new TimelineCard();

        // when
        timelineCard1.setId(1L);
        timelineCard2.setId(1L);

        // then
        assertThat(timelineCard1).isEqualTo(timelineCard2);
    }

    @Test
    void 타임라인_카드_종류_전체_정의값_검증() {
        // given & when
        TimelineCardType[] values = TimelineCardType.values();

        // then
        assertThat(values).containsExactlyInAnyOrder(
                TimelineCardType.DAY,
                TimelineCardType.WEEK,
                TimelineCardType.MONTH
        );
    }

    @Test
    void 문자열로부터_enum_변환_검증() {
        // given
        String input1 = "DAY";
        String input2 = "WEEK";
        String input3 = "MONTH";

        // when
        TimelineCardType result1 = TimelineCardType.valueOf(input1);
        TimelineCardType result2 = TimelineCardType.valueOf(input2);
        TimelineCardType result3 = TimelineCardType.valueOf(input3);

        // then
        assertThat(result1).isEqualTo(TimelineCardType.DAY);
        assertThat(result2).isEqualTo(TimelineCardType.WEEK);
        assertThat(result3).isEqualTo(TimelineCardType.MONTH);
    }

    @Test
    void 잘못된_문자열이_enum_변환에_실패_검증() {
        // given
        String invalidInput = "INVALID";

        // when & then
        assertThatThrownBy(() -> TimelineCardType.valueOf(invalidInput))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 타임라인_카드와_뉴스의_다대일_연관관계_검증() {
        // given
        News news1 = new News();
        TimelineCard timelineCard1 = new TimelineCard();
        TimelineCard timelineCard2 = new TimelineCard();

        // when
        timelineCard1.setNews(news1);
        timelineCard2.setNews(news1);

        // then
        assertThat(timelineCard1.getNews()).isEqualTo(news1);
        assertThat(timelineCard2.getNews()).isEqualTo(news1);
    }
}
