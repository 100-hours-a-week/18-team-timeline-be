package com.tamnara.backend.comment.repository;

import com.tamnara.backend.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
        SELECT c FROM Comment c
        WHERE c.news.id = :id
        ORDER BY c.id DESC
    """)
    Page<Comment> findAllByNewsId(@Param("id") Long id, Pageable pageable);

}
