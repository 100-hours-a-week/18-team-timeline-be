package com.tamnara.backend.alarm.repository;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.global.config.JpaConfig;
import com.tamnara.backend.global.config.QuerydslConfig;
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
@Import({JpaConfig.class, QuerydslConfig.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserAlarmRepositoryTest {

    @PersistenceContext private EntityManager em;

    @Autowired private UserAlarmRepository userAlarmRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AlarmRepository alarmRepository;

    User user;
    Alarm alarm;

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

    private UserAlarm createUserAlarm(boolean isChecked) {
        UserAlarm userAlarm = new UserAlarm();
        userAlarm.setIsChecked(isChecked);
        userAlarm.setCheckedAt(isChecked ? LocalDateTime.now() : null);
        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);
        return userAlarm;
    }

    @Test
    void 회원알림_생성_성공_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(false);
        userAlarmRepository.save(userAlarm);

        // when
        Optional<UserAlarm> userAlarmOptional = userAlarmRepository.findById(userAlarm.getId());

        // then
        assertEquals(userAlarm.getId(), userAlarmOptional.get().getId());
    }

    @Test
    void 회원알림_확인_여부_null_불가_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(true);

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
        UserAlarm userAlarm = createUserAlarm(true);

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
        UserAlarm userAlarm = createUserAlarm(true);

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
        UserAlarm userAlarm = createUserAlarm(true);

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
        UserAlarm userAlarm = createUserAlarm(false);
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
        UserAlarm userAlarm = createUserAlarm(false);
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
        UserAlarm userAlarm = createUserAlarm(false);
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
        UserAlarm userAlarm1 = createUserAlarm(false);
        userAlarmRepository.saveAndFlush(userAlarm1);
        UserAlarm userAlarm2 = createUserAlarm(false);
        userAlarmRepository.saveAndFlush(userAlarm2);
        UserAlarm userAlarm3 = createUserAlarm(false);
        userAlarmRepository.saveAndFlush(userAlarm3);
        UserAlarm userAlarm4 = createUserAlarm(false);
        userAlarmRepository.saveAndFlush(userAlarm4);
        UserAlarm userAlarm5 = createUserAlarm(false);
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
    void 회원알림_확인_여부를_조회_검증() {
        // given
        UserAlarm userAlarm1 = createUserAlarm(false);
        userAlarmRepository.saveAndFlush(userAlarm1);
        UserAlarm userAlarm2 = createUserAlarm(true);
        userAlarmRepository.saveAndFlush(userAlarm2);
        em.clear();

        // when
        Boolean result1 = userAlarmRepository.existsByIdAndIsCheckedTrue(userAlarm1.getId());
        Boolean result2 = userAlarmRepository.existsByIdAndIsCheckedTrue(userAlarm2.getId());

        // then
        assertFalse(result1);
        assertTrue(result2);
    }

    @Test
    void 확인하지_않은_회원알림을_확인_처리_검증() {
        // given
        UserAlarm userAlarm = createUserAlarm(false);
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
