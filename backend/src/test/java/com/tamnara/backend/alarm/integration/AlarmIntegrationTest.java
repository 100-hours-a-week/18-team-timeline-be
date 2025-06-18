package com.tamnara.backend.alarm.integration;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.alarm.event.AlarmEvent;
import com.tamnara.backend.alarm.repository.AlarmRepository;
import com.tamnara.backend.alarm.repository.UserAlarmRepository;
import com.tamnara.backend.config.SyncAsyncConfig;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(SyncAsyncConfig.class)
public class AlarmIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private AlarmRepository alarmRepository;
    @Autowired private UserAlarmRepository userAlarmRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userAlarmRepository.deleteAll();
        userRepository.deleteAll();
        alarmRepository.deleteAll();
        em.flush();
        em.clear();
    }

    private User createUser(Long userId) {
        User user = User.builder()
                .email("이메일" + userId)
                .password("비밀번호")
                .username("이름" + userId)
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        userRepository.saveAndFlush(user);
        em.clear();

        return user;
    }

    @Test
    void 알림_전송_검증() throws Exception {
        // given
        User user1 = createUser(1L);
        User user2 = createUser(2L);
        List<Long> receiverIds = List.of(user1.getId(), user2.getId());

        String title = "테스트 알림 제목";
        String content = "테스트 알림 내용";
        AlarmType targetType = AlarmType.NEWS;
        Long targetId = 1L;

        // when
        AlarmEvent event = new AlarmEvent(receiverIds, title, content, targetType, targetId);
        eventPublisher.publishEvent(event);

        // then
        List<Alarm> alarms = alarmRepository.findAll();
        List<UserAlarm> userAlarms = userAlarmRepository.findAll();

        assertThat(alarms).hasSize(1);
        assertThat(alarms.get(0).getTitle()).isEqualTo(title);
        assertThat(userAlarms).hasSize(2);
        assertThat(userAlarms.get(0).getUser().getId()).isEqualTo(user1.getId());
        assertThat(userAlarms.get(1).getUser().getId()).isEqualTo(user2.getId());
    }
}
