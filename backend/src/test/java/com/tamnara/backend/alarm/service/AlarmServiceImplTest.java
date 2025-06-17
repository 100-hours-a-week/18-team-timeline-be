package com.tamnara.backend.alarm.service;

import com.tamnara.backend.alarm.constant.AlarmServiceConstant;
import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.alarm.dto.response.AlarmListResponse;
import com.tamnara.backend.alarm.repository.AlarmRepository;
import com.tamnara.backend.alarm.repository.UserAlarmRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceImplTest {

    @Mock AlarmRepository alarmRepository;
    @Mock UserAlarmRepository userAlarmRepository;
    @Mock UserRepository userRepository;

    @InjectMocks private AlarmServiceImpl alarmServiceImpl;

    User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(user.getEmail()).thenReturn("이메일");
        lenient().when(user.getPassword()).thenReturn("비밀번호");
        lenient().when(user.getUsername()).thenReturn("이름");
        lenient().when(user.getProvider()).thenReturn("LOCAL");
        lenient().when(user.getProviderId()).thenReturn(null);
        lenient().when(user.getRole()).thenReturn(Role.USER);
        lenient().when(user.getState()).thenReturn(State.ACTIVE);
    }

    private Alarm createAlarm(String title, String content, String targetType, Long targetId) {
        Alarm alarm = new Alarm();
        alarm.setTitle(title);
        alarm.setContent(content);
        alarm.setTargetType(targetType == null ? null : AlarmType.valueOf(targetType));
        alarm.setTargetId(targetId);

        return alarm;
    }

    private UserAlarm createUserAlarm(boolean isChecked, Alarm alarm) {
        UserAlarm userAlarm = new UserAlarm();
        userAlarm.setIsChecked(isChecked);
        userAlarm.setCheckedAt(isChecked ? LocalDateTime.now() : null);
        userAlarm.setUser(user);
        userAlarm.setAlarm(alarm);
        return userAlarm;
    }

    @Test
    void 전체_알림_목록_조회_검증() {
        // given
        Alarm alarm1 = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), 1L);
        Alarm alarm2 = createAlarm("제목2", "내용2", AlarmType.POLLS.toString(), 1L);
        Alarm alarm3 = createAlarm("제목3", "내용3", null, null);
        Alarm alarm4 = createAlarm("제목4", "내용4", AlarmType.NEWS.toString(), 2L);
        Alarm alarm5 = createAlarm("제목5", "내용5", AlarmType.POLLS.toString(), 2L);

        UserAlarm userAlarm1 = createUserAlarm(true, alarm1);
        UserAlarm userAlarm2 = createUserAlarm(false, alarm2);
        UserAlarm userAlarm3 = createUserAlarm(false, alarm3);
        UserAlarm userAlarm4 = createUserAlarm(false, alarm4);
        UserAlarm userAlarm5 = createUserAlarm(false, alarm5);

        List<UserAlarm> userAlarmList = List.of(userAlarm1, userAlarm2, userAlarm3, userAlarm4, userAlarm5);
        Page<UserAlarm> userAlarmPage = new PageImpl<>(userAlarmList);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        when(userAlarmRepository.findByUserIdOrderByIdDesc(user.getId(), pageable)).thenReturn(userAlarmPage);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        AlarmListResponse response = alarmServiceImpl.getAllAlarmPageByUserId(user.getId());

        // then
        assertEquals(AlarmServiceConstant.ALARM_RESPONSE_TYPE_ALL, response.getType());
        assertEquals(5, response.getAlarms().size());
        assertEquals(response.getAlarms().get(0).getId(), alarm5.getId());
        assertEquals(response.getAlarms().get(1).getId(), alarm4.getId());
        assertEquals(response.getAlarms().get(2).getId(), alarm3.getId());
        assertEquals(response.getAlarms().get(3).getId(), alarm2.getId());
        assertEquals(response.getAlarms().get(4).getId(), alarm1.getId());
    }

    @Test
    void 전체_알림_목록_조회_시_알림이_존재하지_않아도_성공_검증() {
        // given
        List<UserAlarm> userAlarmList = List.of();
        Page<UserAlarm> userAlarmPage = new PageImpl<>(userAlarmList);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        when(userAlarmRepository.findByUserIdOrderByIdDesc(user.getId(), pageable)).thenReturn(userAlarmPage);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        AlarmListResponse response = alarmServiceImpl.getAllAlarmPageByUserId(user.getId());

        // then
        assertEquals(AlarmServiceConstant.ALARM_RESPONSE_TYPE_ALL, response.getType());
        assertEquals(0, response.getAlarms().size());
    }

    @Test
    void 전체_알림_목록_조회_시_회원이_존재하지_않으면_예외_처리_검증() {
        // given
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            alarmServiceImpl.getAllAlarmPageByUserId(user.getId());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ResponseMessage.USER_NOT_FOUND, exception.getReason());;
    }

    @Test
    void 북마크_알림_목록_조회_검증() {
        // given
        Alarm alarm1 = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), 1L);
        Alarm alarm2 = createAlarm("제목2", "내용2", AlarmType.NEWS.toString(), 1L);
        Alarm alarm3 = createAlarm("제목3", "내용3", AlarmType.NEWS.toString(), 2L);
        Alarm alarm4 = createAlarm("제목4", "내용4", AlarmType.NEWS.toString(), 2L);
        Alarm alarm5 = createAlarm("제목5", "내용5", AlarmType.NEWS.toString(), 3L);

        UserAlarm userAlarm1 = createUserAlarm(true, alarm1);
        UserAlarm userAlarm2 = createUserAlarm(false, alarm2);
        UserAlarm userAlarm3 = createUserAlarm(false, alarm3);
        UserAlarm userAlarm4 = createUserAlarm(false, alarm4);
        UserAlarm userAlarm5 = createUserAlarm(false, alarm5);

        List<UserAlarm> userAlarmList = List.of(userAlarm1, userAlarm2, userAlarm3, userAlarm4, userAlarm5);
        Page<UserAlarm> userAlarmPage = new PageImpl<>(userAlarmList);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        when(userAlarmRepository.findBookmarkAlarms(user.getId(), pageable)).thenReturn(userAlarmPage);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        AlarmListResponse response = alarmServiceImpl.getBookmarkAlarmPageByUserId(user.getId());

        // then
        assertEquals(AlarmServiceConstant.ALARM_RESPONSE_TYPE_BOOKMARK, response.getType());
        assertEquals(5, response.getAlarms().size());
        assertEquals(response.getAlarms().get(0).getId(), alarm5.getId());
        assertEquals(response.getAlarms().get(1).getId(), alarm4.getId());
        assertEquals(response.getAlarms().get(2).getId(), alarm3.getId());
        assertEquals(response.getAlarms().get(3).getId(), alarm2.getId());
        assertEquals(response.getAlarms().get(4).getId(), alarm1.getId());
    }

    @Test
    void 북마크_알림_목록_조회_시_알림이_존재하지_않아도_성공_검증() {
        // given
        List<UserAlarm> userAlarmList = List.of();
        Page<UserAlarm> userAlarmPage = new PageImpl<>(userAlarmList);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        when(userAlarmRepository.findBookmarkAlarms(user.getId(), pageable)).thenReturn(userAlarmPage);
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));

        // when
        AlarmListResponse response = alarmServiceImpl.getBookmarkAlarmPageByUserId(user.getId());

        // then
        assertEquals(AlarmServiceConstant.ALARM_RESPONSE_TYPE_BOOKMARK, response.getType());
        assertEquals(0, response.getAlarms().size());
    }

    @Test
    void 북마크_알림_목록_조회_시_회원이_존재하지_않으면_예외_처리_검증() {
        // given
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            alarmServiceImpl.getBookmarkAlarmPageByUserId(user.getId());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ResponseMessage.USER_NOT_FOUND, exception.getReason());;
    }

    @Test
    void 미확인_알림_확인_처리_검증() {
        // given
        Alarm alarm = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), 1L);
        UserAlarm userAlarm = createUserAlarm(false, alarm);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userAlarmRepository.findById(any(Long.class))).thenReturn(Optional.of(userAlarm));

        // when
        Long alarmId = alarmServiceImpl.checkUserAlarm(1L, user.getId());

        // then
        assertEquals(alarmId, userAlarm.getId());
    }

    @Test
    void 확인한_알림_확인_처리_검증() {
        // given
        Alarm alarm = createAlarm("제목1", "내용1", AlarmType.NEWS.toString(), 1L);
        UserAlarm userAlarm = createUserAlarm(true, alarm);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(userAlarmRepository.findById(any(Long.class))).thenReturn(Optional.of(userAlarm));

        // when
        Long alarmId = alarmServiceImpl.checkUserAlarm(1L, user.getId());

        // then
        assertEquals(alarmId, userAlarm.getId());
    }

    @Test
    void 알림_확인_처리_시_회원이_존재하지_않으면_예외_처리_검증() {
        // given
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            alarmServiceImpl.getAllAlarmPageByUserId(user.getId());
        });

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals(ResponseMessage.USER_NOT_FOUND, exception.getReason());;
    }

    @Test
    void 오래된_알림들_일괄_삭제_검증() {
        // given
        LocalDateTime cutoff = LocalDateTime.now()
                .minusDays(AlarmServiceConstant.ALARM_DELETE_DAYS).truncatedTo(ChronoUnit.SECONDS);

        // when
        alarmServiceImpl.deleteAlarms();

        // then
        verify(alarmRepository, times(1)).deleteAllOlderThan(argThat(actualCutoff ->
                actualCutoff.truncatedTo(ChronoUnit.SECONDS).equals(cutoff)
        ));
    }
}
