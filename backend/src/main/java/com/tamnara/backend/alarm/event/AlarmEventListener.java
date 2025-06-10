package com.tamnara.backend.alarm.event;

import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.alarm.repository.AlarmRepository;
import com.tamnara.backend.alarm.repository.UserAlarmRepository;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AlarmEventListener {

    private final AlarmRepository alarmRepository;
    private final UserAlarmRepository userAlarmRepository;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleAlarmEvent(AlarmEvent event) {
        Alarm alarm = new Alarm();
        alarm.setTitle(event.getTitle());
        alarm.setContent(event.getContent());
        alarm.setTargetType(event.getTargetType());
        alarm.setTargetId(event.getTargetId());
        alarmRepository.save(alarm);

        for (Long userId : event.getReceiverId()) {
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                continue;
            }

            UserAlarm userAlarm = new UserAlarm();
            userAlarm.setUser(user.get());
            userAlarm.setAlarm(alarm);
            userAlarmRepository.save(userAlarm);
        }
    }
}
