package com.tamnara.backend.alarm.service;

import com.tamnara.backend.alarm.constant.AlarmResponseMessage;
import com.tamnara.backend.alarm.constant.AlarmServiceConstant;
import com.tamnara.backend.alarm.domain.Alarm;
import com.tamnara.backend.alarm.domain.UserAlarm;
import com.tamnara.backend.alarm.dto.AlarmCardDTO;
import com.tamnara.backend.alarm.dto.response.AlarmListResponse;
import com.tamnara.backend.alarm.repository.AlarmRepository;
import com.tamnara.backend.alarm.repository.UserAlarmRepository;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final UserAlarmRepository userAlarmRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;

    @Override
    public AlarmListResponse getAllAlarmPageByUserId(Long userId) {
        log.info("[ALARM] getAllAlarmPageByUserId 시작 - userId:{}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[ALARM] getAllAlarmPageByUserId 처리 중 - 회원 조회 성공, userId:{}", userId);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        Page<UserAlarm> userAlarmPage = userAlarmRepository.findByUserIdOrderByIdDesc(userId, pageable);

        log.info("[ALARM] getAllAlarmPageByUserId 완료 - userId:{}", userId);
        return new AlarmListResponse(
                AlarmServiceConstant.ALARM_RESPONSE_TYPE_ALL,
                getAlarmCardDTOList(userAlarmPage)
        );
    }

    @Override
    public AlarmListResponse getBookmarkAlarmPageByUserId(Long userId) {
        log.info("[ALARM] getBookmarkAlarmPageByUserId 시작 - userId:{}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[ALARM] getBookmarkAlarmPageByUserId 처리 중 - 회원 조회 성공, userId:{}", userId);

        Pageable pageable = PageRequest.of(0, AlarmServiceConstant.ALARM_LIST_SIZE);
        Page<UserAlarm> userAlarmPage = userAlarmRepository.findBookmarkAlarms(userId, pageable);

        log.info("[ALARM] getBookmarkAlarmPageByUserId 완료 - userId:{}", userId);
        return new AlarmListResponse(
                AlarmServiceConstant.ALARM_RESPONSE_TYPE_BOOKMARK,
                getAlarmCardDTOList(userAlarmPage)
        );
    }

    @Override
    @Transactional
    public Long checkUserAlarm(Long userAlarmId, Long userId) {
        log.info("[ALARM] checkUserAlarm 시작 - userId:{} userAlarmId:{}", userId, userAlarmId);

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[ALARM] checkUserAlarm 처리 중 - 회원 조회 성공, userId:{} userAlarmId:{}", userId, userAlarmId);

        UserAlarm userAlarm = userAlarmRepository.findById(userAlarmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AlarmResponseMessage.ALARM_NOT_FOUND));
        log.info("[ALARM] checkUserAlarm 처리 중 - 회원 알림 조회 성공, userId:{} userAlarmId:{}", userId, userAlarmId);

        if (!userAlarm.getIsChecked()) {
            userAlarmRepository.checkUserAlarm(userAlarmId, LocalDateTime.now());
        }
        log.info("[ALARM] checkUserAlarm 완료 - userId:{} userAlarmId:{}", userId, userAlarmId);
        return userAlarm.getId();
    }

    @Override
    public void deleteAlarms() {
        log.info("[ALARM] deleteAlarms 시작");
        alarmRepository.deleteAllOlderThan(LocalDateTime.now().minusDays(AlarmServiceConstant.ALARM_DELETE_DAYS));
        log.info("[ALARM] deleteAlarms 완료");
    }


    /**
     * 헬퍼 메서드
     */
    private List<AlarmCardDTO> getAlarmCardDTOList(Page<UserAlarm> userAlarmPage) {
        List<AlarmCardDTO> alarmCardDTOList = new ArrayList<>();

        for (UserAlarm userAlarm : userAlarmPage) {
            Alarm alarm = userAlarm.getAlarm();

            AlarmCardDTO dto = new AlarmCardDTO(
                    userAlarm.getId(),
                    alarm.getTitle(),
                    alarm.getContent(),
                    userAlarm.getIsChecked(),
                    alarm.getCreatedAt(),
                    alarm.getTargetType() == null ? null : alarm.getTargetType().toString(),
                    alarm.getTargetId()
            );
            alarmCardDTOList.add(dto);
        }

        return alarmCardDTOList;
    }
}
