package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByNumAsc();
    Optional<Category> findByName(CategoryType name);
}
