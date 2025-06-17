package com.tamnara.backend.alarm.domain;

import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAlarmTest {

    @Test
    void 회원알림_기본값_초기화_검증() {
        // given & when
        UserAlarm userAlarm = new UserAlarm();

        // then
        assertNull(userAlarm.getId());
        assertFalse(userAlarm.getIsChecked());
        assertNull(userAlarm.getCheckedAt());
        assertNull(userAlarm.getUser());
        assertNull(userAlarm.getAlarm());
    }

    @Test
    void 회원알림_회원과_알림_설정_검증() {
        // given
        UserAlarm userAlarm = new UserAlarm();
        User user = User.builder().build();
        Alarm alarm = new Alarm();

        // when
        LocalDateTime now = LocalDateTime.now();
        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);

        // then
        assertEquals(user, userAlarm.getUser());
        assertEquals(alarm, userAlarm.getAlarm());
    }

    @Test
    void 회원알림_확인_여부와_확인_시간_설정_검증() {
        // given
        UserAlarm userAlarm = new UserAlarm();
        User user = User.builder().build();
        Alarm alarm = new Alarm();

        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);

        // when
        LocalDateTime now = LocalDateTime.now();
        userAlarm.setIsChecked(true);
        userAlarm.setCheckedAt(now);

        // then
        assertTrue(userAlarm.getIsChecked());
        assertEquals(now, userAlarm.getCheckedAt());
    }


    @Test
    void ID가_동일하면_동일한_회원알림_검증() {
        // given
        UserAlarm userAlarm1 = new UserAlarm();
        UserAlarm userAlarm2 = new UserAlarm();

        // when
        userAlarm1.setId(1L);
        userAlarm2.setId(1L);

        // then
        assertEquals(userAlarm1, userAlarm2);
    }
}
