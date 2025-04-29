package com.tamnara.backend.bookmark.repository;

import com.tamnara.backend.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
}
