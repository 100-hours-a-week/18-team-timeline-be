package com.tamnara.backend.comment.domain;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", updatable = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", referencedColumnName = "id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private News news;

    @Column(name = "content", length = 150, nullable = false)
    private String content;

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
