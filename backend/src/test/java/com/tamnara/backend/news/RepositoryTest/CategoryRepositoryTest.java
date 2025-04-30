package com.tamnara.backend.news.RepositoryTest;

import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CategoryRepositoryTest {

    @Autowired CategoryRepository categoryRepository;

    Category economy;
    Category entertainment;
    Category sports;
    Category ktb;

    private Category createCategory(CategoryType type, Long num) {
        Category category = new Category();
        category.setName(type);
        category.setNum(num);
        return category;
    }

    @BeforeEach
    void setUp() {
        economy = createCategory(CategoryType.ECONOMY, 1L);
        entertainment = createCategory(CategoryType.ENTERTAINMENT, 2L);
        sports = createCategory(CategoryType.SPORTS, 3L);
        ktb = createCategory(CategoryType.KTB, 4L);
    }

    @Test
    void 카테고리_생성_테스트() {
        // given
        categoryRepository.save(economy);
        categoryRepository.save(entertainment);
        categoryRepository.save(sports);
        categoryRepository.save(ktb);

        // when
        Category foundEconomy = categoryRepository.findById(economy.getId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Category foundEntertainment = categoryRepository.findById(entertainment.getId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Category foundSports = categoryRepository.findById(sports.getId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        Category foundKtb = categoryRepository.findById(ktb.getId())
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));

        // then
        assertEquals(economy.getName(), foundEconomy.getName());
        assertEquals(economy.getNum(), foundEconomy.getNum());

        assertEquals(entertainment.getName(), foundEntertainment.getName());
        assertEquals(entertainment.getNum(), foundEntertainment.getNum());

        assertEquals(sports.getName(), foundSports.getName());
        assertEquals(sports.getNum(), foundSports.getNum());

        assertEquals(ktb.getName(), foundKtb.getName());
        assertEquals(ktb.getNum(), foundKtb.getNum());
    }

    @Test
    void 카테고리_삭제_테스트() {
        // given
        categoryRepository.save(economy);

        // when
        categoryRepository.deleteById(economy.getId());

        // then
        assertThrows(RuntimeException.class, () -> {
            categoryRepository.findById(economy.getId())
                    .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        });
    }

    @Test
    void 카테고리_전체_정렬_조회_테스트() {
        // given
        categoryRepository.save(ktb);
        categoryRepository.save(economy);
        categoryRepository.save(entertainment);
        categoryRepository.save(sports);

        // when
        List<Category> categoryList = categoryRepository.findAll();

        // then
        assertEquals(economy.getNum(), categoryList.get(0).getNum());

        Category first = categoryList.get(0);
        Category second = categoryList.get(1);
        Category third = categoryList.get(2);
        assertTrue(first.getNum() < second.getNum());
        assertTrue(second.getNum() < third.getNum());
    }
}
