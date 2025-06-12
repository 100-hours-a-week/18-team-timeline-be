package com.tamnara.backend.alarm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.alarm.config.AlarmServiceMockConfig;
import com.tamnara.backend.alarm.constant.AlarmResponseMessage;
import com.tamnara.backend.alarm.constant.AlarmServiceConstant;
import com.tamnara.backend.alarm.domain.AlarmType;
import com.tamnara.backend.alarm.dto.AlarmCardDTO;
import com.tamnara.backend.alarm.dto.response.AlarmListResponse;
import com.tamnara.backend.alarm.service.AlarmService;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlarmController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AlarmServiceMockConfig.class)
@ActiveProfiles("test")
public class AlarmControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private AlarmService alarmService;

    private static final Long USER_ID = 1L;

    private AlarmCardDTO createAlarmCardDTO(Long id, Boolean isChecked, String targetType, Long targetId) {
        return new AlarmCardDTO(
                id,
                "제목",
                "내용",
                isChecked,
                isChecked ? LocalDateTime.now() : null,
                targetType,
                targetId
        );
    }

    @BeforeEach
    void setupSecurityContext() {
        User user = User.builder()
                .id(USER_ID)
                .username("테스트유저")
                .role(Role.USER)
                .build();

        UserDetailsImpl principal = new UserDetailsImpl(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    void 로그아웃_상태에서_알림_카드_목록_조회_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(get("/users/me/alarms"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_알림_카드_목록_조회_검증() throws Exception {
        // given
        AlarmCardDTO alarmCardDTO1 = createAlarmCardDTO(1L, false, AlarmType.NEWS.toString(), 1L);
        AlarmCardDTO alarmCardDTO2 = createAlarmCardDTO(2L, false, AlarmType.NEWS.toString(), 1L);
        AlarmCardDTO alarmCardDTO3 = createAlarmCardDTO(3L, false, AlarmType.POLLS.toString(), 1L);
        AlarmCardDTO alarmCardDTO4 = createAlarmCardDTO(4L, false, AlarmType.POLLS.toString(), 1L);
        AlarmCardDTO alarmCardDTO5 = createAlarmCardDTO(5L, false, null, null);
        List<AlarmCardDTO> allAlarmCardDTOList = List.of(alarmCardDTO1, alarmCardDTO2, alarmCardDTO3, alarmCardDTO4, alarmCardDTO5);
        List<AlarmCardDTO> bookmarkAlarmCardDTOList = List.of(alarmCardDTO1, alarmCardDTO2);

        AlarmListResponse allAlarms = new AlarmListResponse (
                AlarmServiceConstant.ALARM_RESPONSE_TYPE_ALL,
                allAlarmCardDTOList
        );

        AlarmListResponse bookmarkAlarms = new AlarmListResponse (
                AlarmServiceConstant.ALARM_RESPONSE_TYPE_BOOKMARK,
                bookmarkAlarmCardDTOList
        );

        given(alarmService.getAllAlarmPageByUserId(USER_ID)).willReturn(allAlarms);
        given(alarmService.getBookmarkAlarmPageByUserId(USER_ID)).willReturn(bookmarkAlarms);

        // when & then
        mockMvc.perform(get("/users/me/alarms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(AlarmResponseMessage.ALARM_FETCH_SUCCESS))
                .andExpect(jsonPath("$.data[0].type").value(AlarmServiceConstant.ALARM_RESPONSE_TYPE_ALL))
                .andExpect(jsonPath("$.data[1].type").value(AlarmServiceConstant.ALARM_RESPONSE_TYPE_BOOKMARK));

    }

    @Test
    void 로그아웃_상태에서_단일_알림_확인_불가_검증() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        // when & then
        mockMvc.perform(patch("/users/me/alarms/{alarmId}", 1))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ResponseMessage.USER_NOT_CERTIFICATION));
    }

    @Test
    void 로그인_상태에서_단일_알림_확인_검증() throws Exception {
        // given
        Long alarmId = 1L;
        given(alarmService.checkUserAlarm(alarmId, USER_ID)).willReturn(alarmId);

        // when & then
        mockMvc.perform(patch("/users/me/alarms/{alarmId}", alarmId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(AlarmResponseMessage.ALARM_CHECK_SUCCESS))
                .andExpect(jsonPath("$.data").value(alarmId));
    }
}
