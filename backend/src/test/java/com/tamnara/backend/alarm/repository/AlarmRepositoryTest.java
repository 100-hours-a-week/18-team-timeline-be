package com.tamnara.backend.alarm.repository;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.config.TestConfig;
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
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
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
public class AlarmRepositoryTest {

    @PersistenceContext private EntityManager em;

    @Autowired private AlarmRepository alarmRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserAlarmRepository userAlarmRepository;

    private Alarm createAlarm(String title, String content, String targetType, Long targetId) {
        Alarm alarm = new Alarm();
        alarm.setTitle(title);
        alarm.setContent(content);
        alarm.setTargetType(targetType == null ? null : AlarmType.valueOf(targetType));
        alarm.setTargetId(targetId);

        return alarm;
    }

    private UserAlarm createUserAlarm(boolean isChecked, LocalDateTime checkedAt, User user, Alarm alarm) {
        UserAlarm userAlarm = new UserAlarm();
        userAlarm.setIsChecked(isChecked);
        userAlarm.setCheckedAt(checkedAt);
        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);
        return userAlarm;
    }

    @BeforeEach
    void setUp() {
        alarmRepository.deleteAll();

        em.flush();
        em.clear();
    }

    @Test
    void 알림_생성_성공_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);
        em.clear();

        // when
        Optional<Alarm> findAlarm = alarmRepository.findById(alarm.getId());

        // then
        assertEquals(alarm.getId(), findAlarm.get().getId());
    }

    @Test
    void 알림_제목_null_불가_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);

        // when
        alarm.setTitle(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            alarmRepository.saveAndFlush(alarm);
        });
    }

    @Test
    void 알림_제목_길이_제약_검증() {
        // given
        String shortTitle = "가".repeat(14);
        String longTitle = "가".repeat(15);

        // when
        Alarm validAlarm = createAlarm(shortTitle, "내용", AlarmType.NEWS.toString(), 1L);
        Alarm invalidAlarm = createAlarm(longTitle, "내용", AlarmType.NEWS.toString(), 1L);

        // then
        Alarm savedValidAlarm = alarmRepository.saveAndFlush(validAlarm);
        assertNotNull(savedValidAlarm.getId());
        assertEquals(validAlarm.getTitle(), savedValidAlarm.getTitle());

        assertThrows(DataIntegrityViolationException.class, () -> {
            alarmRepository.saveAndFlush(invalidAlarm);
        });
    }

    @Test
    void 알림_내용_null_불가_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);

        // when
        alarm.setTitle(null);

        // then
        assertThrows(DataIntegrityViolationException.class, () -> {
            alarmRepository.saveAndFlush(alarm);
        });
    }

    @Test
    void 알림_내용_길이_제약_검증() {
        // given
        String shortContent = "가".repeat(255);
        String longContent = "가".repeat(256);

        // when
        Alarm validAlarm = createAlarm("제목", shortContent, AlarmType.NEWS.toString(), 1L);
        Alarm invalidAlarm = createAlarm("제목", longContent, AlarmType.NEWS.toString(), 1L);

        // then
        Alarm savedValidAlarm = alarmRepository.saveAndFlush(validAlarm);
        assertNotNull(savedValidAlarm.getId());
        assertEquals(validAlarm.getContent(), savedValidAlarm.getContent());

        assertThrows(DataIntegrityViolationException.class, () -> {
            alarmRepository.saveAndFlush(invalidAlarm);
        });
    }

    @Test
    void 알림_타겟_종류_null_가능_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);

        // when
        alarm.setTargetType(null);
        Alarm savedAlarm = alarmRepository.saveAndFlush(alarm);

        // then
        assertNotNull(savedAlarm.getId());
        assertNull(savedAlarm.getTargetType());
    }

    @Test
    void 알림_타겟_ID_null_가능_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);

        // when
        alarm.setTargetId(null);
        Alarm savedAlarm = alarmRepository.saveAndFlush(alarm);

        // then
        assertNotNull(savedAlarm.getId());
        assertNull(savedAlarm.getTargetId());
    }

    @Test
    void 알림_생성일자_자동_생성_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);
        em.clear();

        // when
        Optional<Alarm> findAlarm = alarmRepository.findById(alarm.getId());

        // then
        assertNotNull(findAlarm.get().getCreatedAt());
    }

    @Test
    void 알림_수정_성공_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);
        em.clear();

        // when
        Alarm findAlarm = alarmRepository.findById(alarm.getId()).get();
        findAlarm.setTitle("새로운 제목");
        findAlarm.setContent("새로운 내용");
        findAlarm.setTargetType(AlarmType.POLLS);
        findAlarm.setTargetId(2L);
        alarmRepository.saveAndFlush(findAlarm);
        em.clear();

        // then
        Alarm updatedAlarm = alarmRepository.findById(alarm.getId()).get();
        assertEquals(updatedAlarm.getTitle(), findAlarm.getTitle());
        assertEquals(updatedAlarm.getContent(), findAlarm.getContent());
        assertEquals(updatedAlarm.getTargetType(), findAlarm.getTargetType());
        assertEquals(updatedAlarm.getTargetId(), findAlarm.getTargetId());
    }

    @Test
    void 알림_삭제_성공_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);
        em.clear();

        // when
        Alarm findAlarm = alarmRepository.findById(alarm.getId()).get();
        alarmRepository.delete(findAlarm);
        em.flush();
        em.clear();

        // then
        assertFalse(alarmRepository.findById(alarm.getId()).isPresent());
    }

    @Test
    void 알림_삭제_시_연관된_회원알림_CASCADE_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);

        User user = User.builder()
                .email("이메일")
                .password("비밀번호")
                .username("이름")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(user);
        em.clear();

        UserAlarm userAlarm1 = createUserAlarm(false, null, user, alarm);
        userAlarmRepository.saveAndFlush(userAlarm1);
        UserAlarm userAlarm2 = createUserAlarm(true, LocalDateTime.now(), user, alarm);
        userAlarmRepository.saveAndFlush(userAlarm2);
        UserAlarm userAlarm3 = createUserAlarm(false, null, user, alarm);
        userAlarmRepository.saveAndFlush(userAlarm3);
        em.clear();

        // when
        alarmRepository.deleteAll();
        em.flush();
        em.clear();

        // then
        assertFalse(userAlarmRepository.findById(userAlarm1.getId()).isPresent());
        assertFalse(userAlarmRepository.findById(userAlarm2.getId()).isPresent());
        assertFalse(userAlarmRepository.findById(userAlarm3.getId()).isPresent());
    }

    @Test
    void 단일_알림_조회_성공_검증() {
        // given
        Alarm alarm = createAlarm("제목", "내용", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm);
        em.clear();

        // when
        Optional<Alarm> findAlarm = alarmRepository.findById(alarm.getId());

        // then
        assertEquals(findAlarm.get().getId(), alarm.getId());
    }

    @Test
    void 타겟_종류와_타겟_ID로_알림_목록_조회_시_ID_내림차순_정렬_검증() {
        // given
        AlarmType targetType = AlarmType.NEWS;
        Long targetId = 1L;

        Alarm alarm1 = createAlarm("제목1", "내용1", targetType.toString(), targetId);
        Alarm alarm2 = createAlarm("제목2", "내용2", targetType.toString(), targetId);
        Alarm alarm3 = createAlarm("제목3", "내용3", targetType.toString(), targetId);
        alarmRepository.saveAndFlush(alarm1);
        alarmRepository.saveAndFlush(alarm2);
        alarmRepository.saveAndFlush(alarm3);
        em.clear();

        // when
        List<Alarm> findAlarmList = alarmRepository.findByTargetTypeAndTargetIdOrderByIdDesc(targetType, targetId);

        // then
        assertEquals(findAlarmList.get(0).getId(), alarm3.getId());
        assertEquals(findAlarmList.get(1).getId(), alarm2.getId());
        assertEquals(findAlarmList.get(2).getId(), alarm1.getId());
    }

    @Test
    void 생성일자가_기준보다_오래된_알림_일괄_삭제_검증() {
        // given
        Alarm alarm1 = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), 1L);
        alarmRepository.saveAndFlush(alarm1);
        em.clear();

        Alarm alarm2 = createAlarm("제목2", "내용2", null, null);
        alarmRepository.saveAndFlush(alarm2);
        em.clear();

        LocalDateTime cutoff;
        try {
            Thread.sleep(1000); // 1초
            cutoff = LocalDateTime.now();
            Thread.sleep(1000); // 1초
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Alarm alarm3 = createAlarm("제목3", "내용3", AlarmType.POLLS.toString(), 2L);
        alarmRepository.saveAndFlush(alarm3);
        em.clear();

        // when
        alarmRepository.deleteAllOlderThan(cutoff);
        em.flush();
        em.clear();

        // then
        assertFalse(alarmRepository.findById(alarm1.getId()).isPresent());
        assertFalse(alarmRepository.findById(alarm2.getId()).isPresent());
        assertTrue(alarmRepository.findById(alarm3.getId()).isPresent());
    }
}
