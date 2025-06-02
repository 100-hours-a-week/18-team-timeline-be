package com.tamnara.backend.news.domain;

import com.tamnara.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName = "id", nullable = true, updatable = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = true)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Category category;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "summary", length = 255, nullable = false)
    private String summary;

    @Column(name = "is_hotissue", nullable = false)
    private Boolean isHotissue = false;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 1L;

    @Column(name = "update_count", nullable = false)
    private Long updateCount = 1L;

    @Column(name = "ratio_posi", nullable = false)
    private Integer ratioPosi = 0;

    @Column(name = "ratio_neut", nullable = false)
    private Integer ratioNeut = 0;

    @Column(name = "ratio_nega", nullable = false)
    private Integer ratioNega = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
