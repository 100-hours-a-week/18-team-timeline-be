package com.tamnara.backend.alarm.event;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.alarm.repository.AlarmRepository;
import com.tamnara.backend.alarm.repository.UserAlarmRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlarmEventListenerTest {

    @Mock private AlarmRepository alarmRepository;
    @Mock private UserAlarmRepository userAlarmRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AlarmEventListener alarmEventListener;

    User user1;
    User user2;

    AlarmEvent eventWithUsers;
    AlarmEvent eventWithoutUsers;

    Alarm alarm;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).build();
        user2 = User.builder().id(2L).build();

        eventWithUsers = new AlarmEvent(
                List.of(user1.getId(), user2.getId()),
                "알림 제목",
                "알림 내용",
                AlarmType.NEWS,
                100L
        );

        eventWithoutUsers = new AlarmEvent(
                List.of(),
                "알림 제목",
                "알림 내용",
                AlarmType.NEWS,
                100L
        );

        alarm = new Alarm();
        alarm.setId(10L);
    }

    @Test
    void 알림_이벤트_수신_시_알림과_회원알림_저장_검증() {
        // given
        Alarm savedAlarm = new Alarm();
        savedAlarm.setId(10L);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        when(alarmRepository.save(any(Alarm.class))).thenReturn(savedAlarm);

        // when
        alarmEventListener.handleAlarmEvent(eventWithUsers);

        // then
        verify(alarmRepository, times(1)).save(any(Alarm.class));
        verify(userAlarmRepository, times(2)).save(any(UserAlarm.class));
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).findById(user2.getId());
    }

    @Test
    void 일부_수신자가_없을_때_알림과_회원알림_저장_검증() {
        // given
        Alarm savedAlarm = new Alarm();
        savedAlarm.setId(10L);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.findById(user2.getId())).thenReturn(Optional.empty());
        when(alarmRepository.save(any(Alarm.class))).thenReturn(savedAlarm);

        // when
        alarmEventListener.handleAlarmEvent(eventWithUsers);

        // then
        verify(alarmRepository, times(1)).save(any(Alarm.class));
        verify(userAlarmRepository, times(1)).save(any(UserAlarm.class));
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).findById(user2.getId());
    }

    @Test
    void 모든_수신자가_없을_때_알림과_회원알림_저장_검증() {
        // given
        Alarm savedAlarm = new Alarm();
        savedAlarm.setId(10L);

        when(userRepository.findById(user1.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user2.getId())).thenReturn(Optional.empty());
        when(alarmRepository.save(any(Alarm.class))).thenReturn(savedAlarm);

        // when
        alarmEventListener.handleAlarmEvent(eventWithUsers);

        // then
        verify(alarmRepository, times(1)).save(any(Alarm.class));
        verify(userAlarmRepository, never()).save(any(UserAlarm.class));
        verify(userRepository, times(1)).findById(user1.getId());
        verify(userRepository, times(1)).findById(user2.getId());
    }

    @Test
    void 빈_수신자_리스트가_반환되었을_때_알림과_회원알림_저장_검증() {
        // given
        Alarm savedAlarm = new Alarm();
        savedAlarm.setId(10L);

        when(alarmRepository.save(any(Alarm.class))).thenReturn(savedAlarm);

        // when
        alarmEventListener.handleAlarmEvent(eventWithUsers);

        // then
        verify(alarmRepository, times(1)).save(any(Alarm.class));
        verify(userAlarmRepository, never()).save(any(UserAlarm.class));
    }

    @Test
    void 알림_저장_실패_시_예외_처리_검증() {
        // given
        when(alarmRepository.save(any(Alarm.class)))
                .thenThrow(new RuntimeException("알림 저장 실패"));

        // when & then
        assertThrows(RuntimeException.class, () ->
                alarmEventListener.handleAlarmEvent(eventWithUsers)
        );

        verify(alarmRepository, times(1)).save(any(Alarm.class));
        verify(userAlarmRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }
}
