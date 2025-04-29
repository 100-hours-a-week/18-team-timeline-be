package com.tamnara.backend.bookmark.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bookmark", indexes = @Index(name = "idx_user_id_id_desc", columnList = "user_id, id DESC"))
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    //    @OnDelete(action = OnDeleteAction.SET_NULL)
    //    private User user;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "news_id", referencedColumnName = "id", nullable = false, updatable = false)
    //    @OnDelete(action = OnDeleteAction.CASCADE)
    //    private News news;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
