package com.tamnara.backend.poll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.global.util.CreateUserUtil;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.*;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.poll.service.VoteService;
import com.tamnara.backend.poll.service.VoteStatisticsService;
import com.tamnara.backend.poll.util.PollCreateRequestTestBuilder;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_SCHEDULED;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PollController.class)
class PollControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PollService pollService;
    @MockBean private VoteService voteService;
    @MockBean private VoteStatisticsService voteStatisticsService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("투표 생성 API 요청에 성공한다")
    void createPoll_success() throws Exception {
        User user = CreateUserUtil.createActiveAdmin();

        PollCreateRequest request = PollCreateRequestTestBuilder.build("Test Poll", 1, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                List.of(new PollOptionCreateRequest("Option 1", null),
                        new PollOptionCreateRequest("Option 2", null)));

        when(pollService.createPoll(any())).thenReturn(1L);

        mockMvc.perform(post("/polls")
                        .with(csrf())
                        .with(user(new UserDetailsImpl(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 조회 API 요청에 성공한다")
    void getPollInfo_success() throws Exception {
        User user = CreateUserUtil.createActiveUser("testuser@test.com", "testuser", "KAKAO", "11111");
        
        Poll poll = mock(Poll.class);
        
        when(poll.getState()).thenReturn(PollState.PUBLISHED);
        when(pollService.getPollById(1L)).thenReturn(poll);

        mockMvc.perform(get("/polls/1")
                        .with(user(new UserDetailsImpl(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 제출 API 요청에 성공한다")
    @WithMockUser
    void vote_success() throws Exception {
        User user = CreateUserUtil.createActiveUser("testuser@test.com", "testuser", "KAKAO", "11111");

        VoteRequest request = new VoteRequest(List.of(1L, 2L));

        mockMvc.perform(post("/polls/1/vote")
                        .with(csrf())
                        .with(user(new UserDetailsImpl(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 통계 조회 API 요청에 성공한다")
    @WithMockUser
    void getPollStatistics_success() throws Exception {
        User user = CreateUserUtil.createActiveUser("testuser@test.com", "testuser", "KAKAO", "11111");

        PollStatisticsResponse response = new PollStatisticsResponse(1L, List.of(), 0);

        when(voteStatisticsService.getPollStatistics(1L)).thenReturn(response);

        mockMvc.perform(get("/polls/1/stats")
                        .with(user(new UserDetailsImpl(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 스케줄링 API 요청에 성공한다")
    @WithMockUser(roles = "ADMIN")
    void schedule_success() throws Exception {
        User user = CreateUserUtil.createActiveAdmin();
        Poll poll = Mockito.mock(Poll.class);

        given(pollService.getPollById(1L)).willReturn(poll);
        willDoNothing().given(pollService).schedulePoll(poll);

        // when & then
        mockMvc.perform(post("/polls/1/schedule")
                        .with(csrf())
                        .with(user(new UserDetailsImpl(user)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(POLL_SCHEDULED));
    }
}
