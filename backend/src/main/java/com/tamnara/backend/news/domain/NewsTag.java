package com.tamnara.backend.news.domain;

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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
@Table(name = "news_tag")
public class NewsTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", referencedColumnName = "id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private News news;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    private Tag tag;
}
