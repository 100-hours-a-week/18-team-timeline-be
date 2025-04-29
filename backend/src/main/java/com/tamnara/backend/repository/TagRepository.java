package com.tamnara.backend.repository;

import com.tamnara.backend.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM Tag t
        WHERE NOT EXISTS (
            SELECT 1 FROM NewsTag nt
            WHERE nt.tag = t
        )
    """)
    void deleteTagsNotLinkedToNews();
}
