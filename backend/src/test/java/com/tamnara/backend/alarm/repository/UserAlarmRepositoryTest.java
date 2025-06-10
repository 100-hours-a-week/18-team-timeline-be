package com.tamnara.backend.alarm.repository;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.bookmark.domain.Bookmark;
import com.tamnara.backend.bookmark.repository.BookmarkRepository;
import com.tamnara.backend.config.TestConfig;
import com.tamnara.backend.news.domain.News;
import com.tamnara.backend.news.repository.NewsRepository;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserAlarmRepositoryTest {

    @PersistenceContext private EntityManager em;

    @Autowired private UserAlarmRepository userAlarmRepository;
    @Autowired private AlarmRepository alarmRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private NewsRepository newsRepository;
    @Autowired private BookmarkRepository bookmarkRepository;

    User user;
    Alarm alarm;
    News news;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        alarmRepository.deleteAll();
        userAlarmRepository.deleteAll();

        user = User.builder()
                .email("이메일")
                .password("비밀번호")
                .username("이름")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(user);

        alarm = new Alarm();
        alarm.setTitle("제목");
        alarm.setContent("내용");
        alarm.setTargetType(AlarmType.NEWS);
        alarm.setTargetId(1L);
        alarmRepository.saveAndFlush(alarm);

        em.clear();
    }

    private UserAlarm createUserAlarm(Alarm alarm, boolean isChecked) {
        UserAlarm userAlarm = new UserAlarm();
        userAlarm.setIsChecked(isChecked);
        userAlarm.setCheckedAt(isChecked ? LocalDateTime.now() : null);
        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);
        return userAlarm;
    }

    private Alarm createAlarm(String title, String content, String targetType, Long targetId) {
        Alarm alarm = new Alarm();
        alarm.setTitle(title);
        alarm.setContent(content);
        alarm.setTargetType(targetType == null ? null : AlarmType.valueOf(targetType));
        alarm.setTargetId(targetId);

        return alarmRepository.saveAndFlush(alarm);
    }

    private News createNews() {
        news = new News();
        news.setTitle("제목");
        news.setSummary("미리보기 내용");
        news.setIsHotissue(true);
        return newsRepository.saveAndFlush(news);
    }

    private Bookmark createBookmark(News news) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNews(news);
        return bookmarkRepository.saveAndFlush(bookmark);
    }

    @Test
    void 회원알림_생성_성공_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, false);
        userAlarmRepository.save(userAlarm);

        // when
        Optional<UserAlarm> userAlarmOptional = userAlarmRepository.findById(userAlarm.getId());

        // then
        assertEquals(userAlarm.getId(), userAlarmOptional.get().getId());
    }

    @Test
    void 회원알림_확인_여부_null_불가_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, true);

        // when
        userAlarm.setIsChecked(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userAlarmRepository.saveAndFlush(userAlarm);
        });
    }

    @Test
    void 회원알림_확인_시간_null_가능_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, true);

        // when
        userAlarm.setCheckedAt(null);
        UserAlarm savedUserAlarm = userAlarmRepository.saveAndFlush(userAlarm);
        em.clear();

        // then
        assertNotNull(savedUserAlarm.getId());
        assertNull(savedUserAlarm.getCheckedAt());
    }

    @Test
    void 회원알림_회원_필드_null_불가_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, true);

        // when
        userAlarm.setUser(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userAlarmRepository.saveAndFlush(userAlarm);
        });
    }

    @Test
    void 회원알림_알림_필드_null_불가_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, true);

        // when
        userAlarm.setAlarm(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            userAlarmRepository.saveAndFlush(userAlarm);
        });
    }

    @Test
    void 회원알림_확인_상태_업데이트_성공_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm);
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        UserAlarm savedUserAlarm = userAlarmRepository.findById(userAlarm.getId()).get();
        savedUserAlarm.setIsChecked(true);
        savedUserAlarm.setCheckedAt(now);
        UserAlarm updatedUserAlarm = userAlarmRepository.saveAndFlush(savedUserAlarm);
        em.clear();

        // then
        assertEquals(userAlarm.getId(), updatedUserAlarm.getId());
        assertTrue(updatedUserAlarm.getIsChecked());
        assertEquals(now, updatedUserAlarm.getCheckedAt());
    }

    @Test
    void 회원알림_삭제_성공_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm);
        em.clear();

        // when
        userAlarmRepository.delete(userAlarm);

        // then
        assertFalse(userAlarmRepository.findById(userAlarm.getId()).isPresent());
    }

    @Test
    void 단일_회원알림_조회_성공_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm);
        em.clear();

        // when
        UserAlarm findUserAlarm = userAlarmRepository.findById(userAlarm.getId()).get();

        // then
        assertEquals(userAlarm.getId(), findUserAlarm.getId());
    }

    @Test
    void 회원_ID로_회원_알림_목록_조회_시_페이징과_ID_내림차순_정렬_검증() {
        // given
        UserAlarm userAlarm1 = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm1);
        UserAlarm userAlarm2 = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm2);
        UserAlarm userAlarm3 = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm3);
        UserAlarm userAlarm4 = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm4);
        UserAlarm userAlarm5 = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm5);
        em.clear();

        // when
        int pageSize = 3;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<UserAlarm> userAlarmPage = userAlarmRepository.findByUserIdOrderByIdDesc(user.getId(), pageable);
        List<UserAlarm> userAlarmList = userAlarmPage.getContent();

        // then
        assertEquals(5, userAlarmPage.getTotalElements());
        assertEquals(pageSize, userAlarmList.size());
        assertEquals(userAlarm5.getId(), userAlarmList.get(0).getId());
        assertEquals(userAlarm4.getId(), userAlarmList.get(1).getId());
        assertEquals(userAlarm3.getId(), userAlarmList.get(2).getId());
    }

    @Test
    void 북마크_알림_목록_조회_시_페이징과_ID_내림차순_정렬과_타겟_종류_검증() {
        News news1 = createNews();
        News news2 = createNews();
        em.clear();

        Bookmark bookmark1 = createBookmark(news1);
        Bookmark bookmark2 = createBookmark(news2);

        Alarm alarm1 = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), bookmark1.getNews().getId());
        Alarm alarm2 = createAlarm("제목2", "내용2", AlarmType.NEWS.toString(), bookmark2.getNews().getId());
        Alarm alarm3 = createAlarm("제목3", "내용3", AlarmType.NEWS.toString(), bookmark1.getNews().getId());
        Alarm alarm4 = createAlarm("제목4", "내용4", AlarmType.NEWS.toString(), bookmark2.getNews().getId());
        Alarm alarm5 = createAlarm("제목5", "내용5", AlarmType.NEWS.toString(), bookmark1.getNews().getId());
        em.clear();

        UserAlarm userAlarm1 = createUserAlarm(alarm1, false);
        userAlarmRepository.saveAndFlush(userAlarm1);
        UserAlarm userAlarm2 = createUserAlarm(alarm2, false);
        userAlarmRepository.saveAndFlush(userAlarm2);
        UserAlarm userAlarm3 = createUserAlarm(alarm3, false);
        userAlarmRepository.saveAndFlush(userAlarm3);
        UserAlarm userAlarm4 = createUserAlarm(alarm4, false);
        userAlarmRepository.saveAndFlush(userAlarm4);
        UserAlarm userAlarm5 = createUserAlarm(alarm5, false);
        userAlarmRepository.saveAndFlush(userAlarm5);
        em.clear();

        // when
        int pageSize = 3;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<UserAlarm> bookmarkAlarmPage = userAlarmRepository.findBookmarkAlarms(user.getId(), pageable);
        List<UserAlarm> bookmarkAlarmList = bookmarkAlarmPage.getContent();

        // then
        assertEquals(5, bookmarkAlarmPage.getTotalElements());
        assertEquals(pageSize, bookmarkAlarmList.size());
        assertEquals(userAlarm5.getId(), bookmarkAlarmList.get(0).getId());
        assertEquals(userAlarm4.getId(), bookmarkAlarmList.get(1).getId());
        assertEquals(userAlarm3.getId(), bookmarkAlarmList.get(2).getId());

        assertEquals(AlarmType.NEWS, userAlarm5.getAlarm().getTargetType());
        assertEquals(AlarmType.NEWS, userAlarm4.getAlarm().getTargetType());
        assertEquals(AlarmType.NEWS, userAlarm3.getAlarm().getTargetType());

        assertEquals(userAlarm5.getAlarm().getTargetId(), bookmark1.getNews().getId());
        assertEquals(userAlarm4.getAlarm().getTargetId(), bookmark2.getNews().getId());
        assertEquals(userAlarm3.getAlarm().getTargetId(), bookmark1.getNews().getId());
    }

    @Test
    void 확인하지_않은_회원알림을_확인_처리_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(alarm, false);
        userAlarmRepository.saveAndFlush(userAlarm);
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        userAlarmRepository.checkUserAlarm(userAlarm.getId(), now);
        em.clear();

        // then
        UserAlarm savedUserAlarm = userAlarmRepository.findById(userAlarm.getId()).get();
        assertEquals(userAlarm.getId(), savedUserAlarm.getId());
        assertTrue(savedUserAlarm.getIsChecked());
        assertEquals(now, savedUserAlarm.getCheckedAt());
    }
}
