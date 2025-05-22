package com.tamnara.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.UserUpdateRequestDto;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtProvider jwtProvider;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .username("testuser")
                .provider("LOCAL")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
        user = userRepository.save(user);
    }

    private String getAccessToken(User user) {
        return "Bearer " + jwtProvider.createAccessToken(user);
    }

    @Test
    @DisplayName("회원 정보 조회 통합 테스트")
    void getCurrentUser_success() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", getAccessToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.user.username", is("testuser")));
    }

    @Test
    @DisplayName("닉네임 수정 통합 테스트")
    void updateNickname_success() throws Exception {
        UserUpdateRequestDto dto = new UserUpdateRequestDto("newnick");

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", getAccessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assert updated.getUsername().equals("newnick");
    }

    @Test
    @DisplayName("닉네임 중복 수정 실패")
    void updateNickname_conflict() throws Exception {
        // 이미 존재하는 닉네임으로 저장
        userRepository.save(User.builder()
                .email("another@example.com")
                .username("dupeNick")
                .provider("KAKAO")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build());

        UserUpdateRequestDto dto = new UserUpdateRequestDto("dupeNick");

        mockMvc.perform(patch("/users/me")
                        .header("Authorization", getAccessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }
}
