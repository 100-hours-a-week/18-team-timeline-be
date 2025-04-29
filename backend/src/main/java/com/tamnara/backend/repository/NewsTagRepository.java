package com.tamnara.backend.repository;

import com.tamnara.backend.domain.NewsTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsTagRepository extends JpaRepository<NewsTag, Long> {
}
