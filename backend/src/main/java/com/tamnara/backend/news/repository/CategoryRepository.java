package com.tamnara.backend.news.repository;

import com.tamnara.backend.news.domain.Category;
import com.tamnara.backend.news.domain.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
        SELECT c FROM Category c
        ORDER BY c.num ASC
    """)
    List<Category> findAll();

    Optional<Category> findByName(CategoryType name);
}
