package com.tamnara.backend.poll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.global.util.CreateUserUtil;
import com.tamnara.backend.poll.config.PollTestConfig;
import com.tamnara.backend.poll.domain.Poll;
import com.tamnara.backend.poll.domain.PollState;
import com.tamnara.backend.poll.dto.request.PollCreateRequest;
import com.tamnara.backend.poll.dto.request.PollOptionCreateRequest;
import com.tamnara.backend.poll.dto.request.VoteRequest;
import com.tamnara.backend.poll.dto.response.PollIdResponse;
import com.tamnara.backend.poll.dto.response.PollInfoResponse;
import com.tamnara.backend.poll.service.PollService;
import com.tamnara.backend.poll.service.VoteService;
import com.tamnara.backend.poll.util.PollCreateRequestTestBuilder;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.tamnara.backend.poll.constant.PollResponseMessage.POLL_SCHEDULED;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PollController.class)
@Import(PollTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PollControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private PollService pollService;
    @Autowired private VoteService voteService;

    User user;
    @BeforeEach
    void setupSecurityContext() {
        user = User.builder()
                .id(1L)
                .username("관리자")
                .role(Role.ADMIN)
                .state(State.ACTIVE)
                .build();

        UserDetailsImpl principal = new UserDetailsImpl(user);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @Test
    @DisplayName("투표 생성 API 요청에 성공한다")
    void createPoll_success() throws Exception {
        PollCreateRequest request = PollCreateRequestTestBuilder.build("Test Poll", 1, 1,
                List.of(new PollOptionCreateRequest("Option 1", null),
                        new PollOptionCreateRequest("Option 2", null)));

        when(pollService.createPoll(any())).thenReturn(1L);

        mockMvc.perform(post("/polls")
                        .with(csrf())
                        .with(user(new UserDetailsImpl(user)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 조회 API 요청에 성공한다")
    void getPollInfo_success() throws Exception {
        Poll poll = mock(Poll.class);
        
        when(poll.getState()).thenReturn(PollState.PUBLISHED);
        when(pollService.getLatestPublishedPoll(user.getId())).thenReturn(any(PollInfoResponse.class));

        mockMvc.perform(get("/polls")
                        .with(user(new UserDetailsImpl(user))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 제출 API 요청 성공")
    void vote_success() throws Exception {
        // given
        VoteRequest voteRequest = new VoteRequest(List.of(1L, 2L));
        given(voteService.vote(user, voteRequest)).willReturn(new PollIdResponse(1L));

        // when & then

        mockMvc.perform(post("/polls/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("투표 스케줄링 API 요청 성공")
    void schedule_success() throws Exception {
        User user = CreateUserUtil.createActiveAdmin();
        Poll poll = mock(Poll.class);
        given(pollService.getPollById(anyLong())).willReturn(poll);

        // when & then
        mockMvc.perform(post("/polls/{pollId}/schedule", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(POLL_SCHEDULED));
    }
}
