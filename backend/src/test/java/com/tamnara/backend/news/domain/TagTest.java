package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TagTest {

    @Test
    void 태그_초기화_검증() {
        // given & when
        Tag tag = new Tag();

        // then
        assertThat(tag.getName()).isNull();
    }

    @Test
    void 태그_이름_설정_검증() {
        // given
        Tag tag = new Tag();

        // when
        tag.setName("테스트");

        // then
        assertThat(tag.getName()).isEqualTo("테스트");
    }

    @Test
    void ID가_동일하면_동일한_태그_검증() {
        // given
        Tag tag1 = new Tag();
        Tag tag2 = new Tag();

        // when
        tag1.setId(1L);
        tag2.setId(1L);

        // then
        assertThat(tag1).isEqualTo(tag2);
    }
}
