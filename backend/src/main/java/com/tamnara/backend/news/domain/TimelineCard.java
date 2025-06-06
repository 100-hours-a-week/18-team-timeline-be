package com.tamnara.backend.news.domain;

import com.tamnara.backend.news.domain.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(of = "id")
@Table(name = "timeline_cards", indexes = @Index(name = "idx_news_start_desc", columnList = "news_id, start_at DESC"))
public class TimelineCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private News news;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Convert(converter = StringListConverter.class)
    @Column(name = "source", columnDefinition = "json", nullable = true)
    private List<String> source;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration", length = 10, nullable = false)
    private TimelineCardType duration = TimelineCardType.DAY;

    @Column(name = "start_at", nullable = false)
    private LocalDate startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDate endAt;
}
