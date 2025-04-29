package com.tamnara.backend.repository;

import com.tamnara.backend.domain.NewsImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsImageRepository extends JpaRepository<NewsImage, Long> {
}
