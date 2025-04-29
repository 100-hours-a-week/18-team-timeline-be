package com.tamnara.backend.comment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments", indexes = @Index(name = "idx_news_id_id_desc", columnList = "news_id, id DESC"))
public class Comment {
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

    @Column(name = "content", length = 150, nullable = false)
    private String content;

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
