package com.tamnara.backend.news.RepositoryTest;

import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.domain.TimelineCard;
import com.tamnara.backend.news.domain.TimelineCardType;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.news.repository.TimelineCardRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TimelineCardRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private TimelineCardRepository timelineCardRepository;
    @Autowired private NewsRepository newsRepository;

    News news;
    TimelineCard timelineCard1;
    TimelineCard timelineCard2;
    TimelineCard timelineCard3;

    @BeforeEach
    public void setUp() {
        news = new News();
        news.setTitle("24자의 제목");
        news.setSummary("36자의 미리보기 내용");
        news.setIsHotissue(true);
        news.setCategory(null);
        newsRepository.save(news);

        List<String> source = new ArrayList<>();
        source.add("source1");
        source.add("source2");

        timelineCard1 = new TimelineCard();
        timelineCard1.setTitle("18자의 타임라인 카드");
        timelineCard1.setContent("타임라인 카드의 내용");
        timelineCard1.setSource(source);
        timelineCard1.setDuration(TimelineCardType.DAY);
        timelineCard1.setStartAt(LocalDate.of(2025, 4, 11));
        timelineCard1.setEndAt(LocalDate.of(2025, 4, 11));
        timelineCard1.setNews(news);

        timelineCard2 = new TimelineCard();
        timelineCard2.setTitle("18자의 타임라인 카드");
        timelineCard2.setContent("타임라인 카드의 내용");
        timelineCard2.setSource(source);
        timelineCard2.setDuration(TimelineCardType.WEEK);
        timelineCard2.setStartAt(LocalDate.of(2025, 4, 3));
        timelineCard2.setEndAt(LocalDate.of(2025, 4, 10));
        timelineCard2.setNews(news);

        timelineCard3 = new TimelineCard();
        timelineCard3.setTitle("18자의 타임라인 카드");
        timelineCard3.setContent("타임라인 카드의 내용");
        timelineCard3.setSource(source);
        timelineCard3.setDuration(TimelineCardType.MONTH);
        timelineCard3.setStartAt(LocalDate.of(2025, 3, 2));
        timelineCard3.setEndAt(LocalDate.of(2025, 4, 2));
        timelineCard3.setNews(news);
    }

    @Test
    public void 단일_타임라인카드_생성_테스트() {
        // given
        timelineCardRepository.save(timelineCard1);

        // when
        Optional<TimelineCard> findTimelineCard = timelineCardRepository.findById(timelineCard1.getId());

        // then
        assertEquals(timelineCard1.getId(), findTimelineCard.get().getId());
    }

    @Test
    public void 단일_타임라인카드_삭제_테스트() {
        // given
        timelineCardRepository.save(timelineCard1);

        // when
        Optional<TimelineCard> findTimelineCard = timelineCardRepository.findById(timelineCard1.getId());
        timelineCardRepository.delete(findTimelineCard.get());

        // then
        assertFalse(timelineCardRepository.findById(timelineCard1.getId()).isPresent());
    }

    @Test
    public void 뉴스_삭제시_연관_타임라인카드들_자동_삭제_테스트() {
        // given
        timelineCardRepository.save(timelineCard1);
        timelineCardRepository.save(timelineCard2);
        timelineCardRepository.save(timelineCard3);

        em.flush();
        em.clear();

        // when
        newsRepository.delete(news);

        em.flush();
        em.clear();

        // then
        assertFalse(newsRepository.findById(news.getId()).isPresent());
        assertEquals(0, timelineCardRepository.findAll().size());
    }

    @Test
    public void 뉴스_연관_전체_타임라인카드들_정렬_조회_테스트() {
        // given
        timelineCardRepository.save(timelineCard3);
        timelineCardRepository.save(timelineCard2);
        timelineCardRepository.save(timelineCard1);

        // when
        List<TimelineCard> timelineCardsList = timelineCardRepository.findAllByNewsIdAndDuration(news.getId(), null);

        // then
        assertEquals(3, timelineCardsList.size());
        assertTrue(timelineCardsList.stream().allMatch(t -> t.getNews().getId().equals(news.getId())));

        TimelineCard first = timelineCardsList.get(0);
        TimelineCard second = timelineCardsList.get(1);
        TimelineCard third = timelineCardsList.get(2);
        assertTrue(first.getStartAt().isAfter(second.getStartAt()));
        assertTrue(second.getStartAt().isAfter(third.getStartAt()));
    }

    @Test
    public void 뉴스_연관_타입별_타임라인카드들_정렬_조회_테스트() {
        // given
        TimelineCardType duration = TimelineCardType.WEEK;
        timelineCard1.setDuration(duration);
        timelineCard2.setDuration(duration);
        timelineCard3.setDuration(duration);

        timelineCardRepository.save(timelineCard1);
        timelineCardRepository.save(timelineCard2);
        timelineCardRepository.save(timelineCard3);

        // when
        List<TimelineCard> timelineCardsWeekList = timelineCardRepository.findAllByNewsIdAndDuration(news.getId(), duration);

        // then
        assertEquals(3, timelineCardsWeekList.size());
        assertTrue(timelineCardsWeekList.stream().allMatch(t -> t.getNews().getId().equals(news.getId())));
        assertTrue(timelineCardsWeekList.stream().allMatch(t -> t.getDuration().equals(duration)));

        TimelineCard first = timelineCardsWeekList.get(0);
        TimelineCard second = timelineCardsWeekList.get(1);
        TimelineCard third = timelineCardsWeekList.get(2);
        assertTrue(first.getStartAt().isAfter(second.getStartAt()));
        assertTrue(second.getStartAt().isAfter(third.getStartAt()));
    }
}
