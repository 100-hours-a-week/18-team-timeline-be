package com.tamnara.backend.bookmark.repository;

import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAndNews(User user, News news);
    Page<Bookmark> findByUser(User user, Pageable pageable);

    @Query("SELECT b.user.id FROM Bookmark b WHERE b.news = :news")
    List<Long> findUsersByNews(@Param("news") News news);
}
