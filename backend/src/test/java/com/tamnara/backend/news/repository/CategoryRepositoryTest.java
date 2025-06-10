package com.tamnara.backend.news.repository;

import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.global.config.QuerydslConfig;
import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaConfig.class, QuerydslConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private NewsRepository newsRepository;

    private Category createCategory(CategoryType type, Long num) {
        Category category = new Category();
        category.setName(type);
        category.setNum(num);
        return category;
    }

    @BeforeEach
    void setUp() {
        newsRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryRepository.flush();
    }

    @Test
    void 존재하지_않는_카테고리타입_파싱_예외_발생_검증() {
        // given
        String invalidType = "INVALID";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            CategoryType.valueOf(invalidType);
        });
    }

    @Test
    void 카테고리_이름_null_불가_검증() {
        // given
        Category category = new Category();

        // when
        category.setName(null);
        category.setNum(50L);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.saveAndFlush(category);
        });
    }

    @Test
    void 카테고리_이름_유일성_검증() {
        // given
        Category category1 = createCategory(CategoryType.ECONOMY, 50L);
        categoryRepository.saveAndFlush(category1);

        // when
        Category category2 = createCategory(CategoryType.ECONOMY, 51L);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.saveAndFlush(category2);
        });
    }

    @Test
    void 카테고리_순서_null_불가_검증() {
        // given
        Category category = new Category();
        CategoryType name = CategoryType.ECONOMY;

        // when
        category.setName(name);
        category.setNum(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.saveAndFlush(category);
        });
    }

    @Test
    void 카테고리_순서_유일성_검증() {
        // given
        Category category1 = createCategory(CategoryType.ECONOMY, 50L);
        categoryRepository.saveAndFlush(category1);

        // when
        Category category2 = createCategory(CategoryType.KTB, 50L);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.saveAndFlush(category2);
        });
    }

    @Test
    void 카테고리_조회_시_전체_num_오름차순_정렬_검증() {
        // given
        Category economy = createCategory(CategoryType.ECONOMY, 1L);
        Category entertainment = createCategory(CategoryType.ENTERTAINMENT, 2L);
        Category sports = createCategory(CategoryType.SPORTS, 3L);
        Category ktb = createCategory(CategoryType.KTB, 4L);

        categoryRepository.save(economy);
        categoryRepository.save(entertainment);
        categoryRepository.save(sports);
        categoryRepository.save(ktb);

        // when
        List<Category> categoryList = categoryRepository.findAllByOrderByNumAsc();
        Category first = categoryList.get(0);
        Category second = categoryList.get(1);
        Category third = categoryList.get(2);
        Category fourth = categoryList.get(3);

        // then
        assertTrue(first.getNum() < second.getNum());
        assertTrue(second.getNum() < third.getNum());
        assertTrue(third.getNum() < fourth.getNum());
    }

    @Test
    void 카테고리_이름으로_단일_조회_검증() {
        // given
        Category ktb = createCategory(CategoryType.KTB, 4L);
        categoryRepository.save(ktb);

        // when
        String name = "KTB";
        CategoryType categoryType = CategoryType.valueOf(name);
        Optional<Category> category = categoryRepository.findByName(categoryType);

        // then
        assertEquals(ktb.getName(), category.get().getName());
    }
}
