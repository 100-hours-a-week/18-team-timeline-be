package com.tamnara.backend.alarm.controller;

import com.tamnara.backend.alarm.constant.AlarmResponseMessage;
import com.tamnara.backend.alarm.dto.response.AlarmListResponse;
import com.tamnara.backend.alarm.service.AlarmService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.dto.WrappedDTO;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.user.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/stream")
    public SseEmitter streamAlarm(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletResponse response) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            response.setContentType("text/event-stream;charset=UTF-8");

            Long userId = userDetails.getUser().getId();

            SseEmitter emitter = new SseEmitter(0L);
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    List<AlarmListResponse> alarmList = new ArrayList<>();
                    alarmList.add(alarmService.getAllAlarmPageByUserId(userId));
                    alarmList.add(alarmService.getBookmarkAlarmPageByUserId(userId));

                    emitter.send(SseEmitter.event()
                            .name("alarm-event")
                            .data(new WrappedDTO<>(
                                    true,
                                    AlarmResponseMessage.ALARM_FETCH_SUCCESS,
                                    alarmList
                            )));

                    emitter.complete();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });

            return emitter;

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<WrappedDTO<List<AlarmListResponse>>> getAlarmPage(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();

            List<AlarmListResponse> alarmList = new ArrayList<>();
            alarmList.add(alarmService.getAllAlarmPageByUserId(userId));
            alarmList.add(alarmService.getBookmarkAlarmPageByUserId(userId));

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            AlarmResponseMessage.ALARM_FETCH_SUCCESS,
                            alarmList
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{alarmId}")
    public ResponseEntity<WrappedDTO<Long>> checkAlarm(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        try {
            if (userDetails == null || userDetails.getUser() == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ResponseMessage.USER_NOT_CERTIFICATION);
            }

            Long userId = userDetails.getUser().getId();
            Long checkedAlarmId = alarmService.checkUserAlarm(alarmId, userId);

            return ResponseEntity.ok().body(
                    new WrappedDTO<>(
                            true,
                            AlarmResponseMessage.ALARM_CHECK_SUCCESS,
                            checkedAlarmId
                    ));

        } catch (ResponseStatusException e) {
            throw new CustomException(HttpStatus.valueOf(e.getStatusCode().value()), e.getReason());
        } catch (IllegalArgumentException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ResponseMessage.BAD_REQUEST);
        } catch (RuntimeException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR);
        }
    }
}
