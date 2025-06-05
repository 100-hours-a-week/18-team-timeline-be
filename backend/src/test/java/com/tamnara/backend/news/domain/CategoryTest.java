package com.tamnara.backend.news.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryTest {

    @Test
    void 카테고리_초기화_검증() {
        // given & when
        Category category = new Category();

        // then
        assertThat(category.getName()).isNull();
        assertThat(category.getNum()).isEqualTo(0);
    }

    @Test
    void 카테고리_이름과_순서_설정_검증() {
        // given
        Category category = new Category();
        String input = "ECONOMY";

        // when
        CategoryType categoryType = CategoryType.valueOf(input);
        category.setName(categoryType);
        category.setNum(1L);

        // then
        assertThat(category.getName()).isEqualTo(CategoryType.ECONOMY);
        assertThat(category.getNum()).isEqualTo(1L);
    }

    @Test
    void 카테고리_전체_정의값_검증() {
        // given & when
        CategoryType[] values = CategoryType.values();

        // then
        assertThat(values).containsExactlyInAnyOrder(
                CategoryType.ECONOMY,
                CategoryType.ENTERTAINMENT,
                CategoryType.SPORTS,
                CategoryType.KTB
        );
    }

    @Test
    void 문자열로부터_enum_변환_검증() {
        // given
        String input1 = "ECONOMY";
        String input2 = "ENTERTAINMENT";
        String input3 = "SPORTS";
        String input4 = "KTB";

        // when
        CategoryType result1 = CategoryType.valueOf(input1);
        CategoryType result2 = CategoryType.valueOf(input2);
        CategoryType result3 = CategoryType.valueOf(input3);
        CategoryType result4 = CategoryType.valueOf(input4);

        // then
        assertThat(result1).isEqualTo(CategoryType.ECONOMY);
        assertThat(result2).isEqualTo(CategoryType.ENTERTAINMENT);
        assertThat(result3).isEqualTo(CategoryType.SPORTS);
        assertThat(result4).isEqualTo(CategoryType.KTB);
    }

    @Test
    void 잘못된_문자열이_enum_변환에_실패_검증() {
        // given
        String invalidInput = "INVALID";

        // when & then
        assertThatThrownBy(() -> CategoryType.valueOf(invalidInput))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
