package com.tamnara.backend.news.repository;

import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TimelineCardRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private TimelineCardRepository timelineCardRepository;
    @Autowired private NewsRepository newsRepository;

    private TimelineCard createTimelineCard(News news, String title, String content, List<String> source, String duration, LocalDate startAt, LocalDate endAt) {
        TimelineCard timelineCard = new TimelineCard();
        timelineCard.setNews(news);
        timelineCard.setTitle(title);
        timelineCard.setContent(content);
        timelineCard.setSource(source);
        if (duration != null) {
            timelineCard.setDuration(TimelineCardType.valueOf(duration));
        }
        timelineCard.setStartAt(startAt);
        timelineCard.setEndAt(endAt);
        return timelineCard;
    }

    News news;
    List<String> source = List.of("source1", "source2");
    LocalDate startAt = LocalDate.of(2019, 1, 1);
    LocalDate endAt = LocalDate.of(2019, 12, 31);

    @BeforeEach
    void setUp() {
        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        newsRepository.saveAndFlush(news);
    }

    @Test
    void 존재하지_않는_타임라인_카드_타입_파싱_예외_발생_검증() {
        // given
        String invalidType = "INVALID";

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            TimelineCardType.valueOf(invalidType);
        });
    }

    @Test
    void 타임라인_카드_생성_성공_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        TimelineCard findTimelineCard = timelineCardRepository.findById(timelineCard.getId()).get();

        // then
        assertEquals(timelineCard.getId(), findTimelineCard.getId());
    }

    @Test
    void 타임라인_카드_제목_null_불가_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        timelineCard.setTitle(null);

        // then
        assertNull(timelineCard.getTitle());
        assertThrows(DataIntegrityViolationException.class, () -> {
            timelineCardRepository.saveAndFlush(timelineCard);
        });
    }

    @Test
    void 타임라인_카드_제목_길이_제한_검증() {
        // given
        String title1 = "가".repeat(255);
        String title2 = "가".repeat(256);

        // when
        TimelineCard timelineCard1 = createTimelineCard(news, title1, "내용", source, "DAY", startAt, endAt);
        TimelineCard timelineCard2 = createTimelineCard(news, title2, "내용", source, "DAY", startAt, endAt);

        // then
        timelineCardRepository.saveAndFlush(timelineCard1);
        assertTrue(timelineCardRepository.existsById(timelineCard1.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> {
            timelineCardRepository.saveAndFlush(timelineCard2);
        });
    }

    @Test
    void 타임라인_카드_내용_null_불가_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        timelineCard.setContent(null);

        // then
        assertNull(timelineCard.getContent());
        assertThrows(DataIntegrityViolationException.class, () -> {
            timelineCardRepository.saveAndFlush(timelineCard);
        });
    }

    @Test
    void 타임라인_카드_내용_길이_제한_검증() {
        // given
        String content1 = "가".repeat(21_845);
        String content2 = "가".repeat(21_846);

        // when
        TimelineCard timelineCard1 = createTimelineCard(news, "제목", content1, source, "DAY", startAt, endAt);
        TimelineCard timelineCard2 = createTimelineCard(news, "제목", content2, source, "DAY", startAt, endAt);

        // then
        timelineCardRepository.saveAndFlush(timelineCard1);
        assertTrue(timelineCardRepository.existsById(timelineCard1.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> {
            timelineCardRepository.saveAndFlush(timelineCard2);
        });
    }

    @Test
    void 타임라인_카드_출처_null_허용_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        timelineCard.setSource(null);
        timelineCardRepository.saveAndFlush(timelineCard);

        // then
        assertNull(timelineCard.getSource());
        assertTrue(timelineCardRepository.existsById(timelineCard.getId()));
    }

    @Test
    void 타임라인_카드_종류_null_불가_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        timelineCard.setDuration(null);

        // then
        assertNull(timelineCard.getDuration());
        assertThrows(DataIntegrityViolationException.class, () -> {
            timelineCardRepository.saveAndFlush(timelineCard);
        });
    }

    @Test
    void 타임라인_카드_삭제_성공_검증() {
        // given
        TimelineCard timelineCard = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard);

        // when
        TimelineCard findTimelineCard = timelineCardRepository.findById(timelineCard.getId()).get();
        timelineCardRepository.delete(findTimelineCard);

        // then
        assertFalse(timelineCardRepository.findById(timelineCard.getId()).isPresent());
    }

    @Test
    void 뉴스_ID로_뉴스와_연관된_타임라인_카드들_전체_삭제_검증() {
        // given
        TimelineCard timelineCard1 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard1);
        TimelineCard timelineCard2 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard2);
        TimelineCard timelineCard3 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard3);

        // when
        timelineCardRepository.deleteAllByNewsId(news.getId());
        em.flush();
        em.clear();

        // then
        assertEquals(0, timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId()).size());
    }

    @Test
    void 뉴스와_연관된_타임라인_카드들_조회_시_시작일_내림차순_정렬_검증() {
        // given
        TimelineCard timelineCard1 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt.minusDays(3), endAt.minusDays(3));
        timelineCardRepository.saveAndFlush(timelineCard1);
        TimelineCard timelineCard2 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt.minusDays(1), endAt.minusDays(1));
        timelineCardRepository.saveAndFlush(timelineCard2);
        TimelineCard timelineCard3 = createTimelineCard(news, "제목", "내용", source, "DAY", startAt, endAt);
        timelineCardRepository.saveAndFlush(timelineCard3);

        // when
        List<TimelineCard> timelineCardsList = timelineCardRepository.findAllByNewsIdOrderByStartAtDesc(news.getId());

        // then
        assertEquals(3, timelineCardsList.size());
        assertTrue(timelineCardsList.stream().allMatch(t -> t.getNews().getId().equals(news.getId())));

        TimelineCard first = timelineCardsList.get(0);
        TimelineCard second = timelineCardsList.get(1);
        TimelineCard third = timelineCardsList.get(2);
        assertTrue(first.getStartAt().isAfter(second.getStartAt()));
        assertTrue(second.getStartAt().isAfter(third.getStartAt()));
    }
}
