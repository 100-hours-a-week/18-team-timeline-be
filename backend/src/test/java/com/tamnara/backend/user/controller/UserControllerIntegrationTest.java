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
    @DisplayName("이메일 중복 조회 - available")
    void checkEmail_available() throws Exception {
        // when & then
        mockMvc.perform(get("/users/check-email")
                        .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 이메일입니다."));
    }

    @Test
    @DisplayName("이메일 중복 조회 - conflict")
    void checkEmail_conflict() throws Exception {
        // given
        userRepository.save(User.builder()
                .email("duplicate@example.com")
                .username("dupeUser")
                .provider("LOCAL")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build());

        // when & then
        mockMvc.perform(get("/users/check-email")
                        .param("email", "duplicate@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    @Test
    @DisplayName("닉네임 중복 조회 - available")
    void checkNickname_available() throws Exception {
        // when & then
        mockMvc.perform(get("/users/check-nickname")
                        .param("nickname", "uniqueNick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.message").value("사용 가능한 닉네임입니다."));
    }

    @Test
    @DisplayName("닉네임 중복 조회 - conflict")
    void checkNickname_conflict() throws Exception {
        // given
        userRepository.save(User.builder()
                .email("another@example.com")
                .username("dupeNick")
                .provider("KAKAO")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build());

        // when & then
        mockMvc.perform(get("/users/check-nickname")
                        .param("nickname", "dupeNick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }

    @Test
    @DisplayName("회원 정보 조회 통합 테스트")
    void getCurrentUser_success() throws Exception {
        // when & then
        mockMvc.perform(get("/users/me")
                        .header("Authorization", getAccessToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.email", is("test@example.com")))
                .andExpect(jsonPath("$.data.user.username", is("testuser")));
    }

    @Test
    @DisplayName("닉네임 수정 통합 테스트")
    void updateNickname_success() throws Exception {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto("newnick");

        // when & then
        mockMvc.perform(patch("/users/me")
                        .header("Authorization", getAccessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // when
        User updated = userRepository.findById(user.getId()).orElseThrow();

        // then
        assert updated.getUsername().equals("newnick");
    }

    @Test
    @DisplayName("닉네임 중복 수정 실패")
    void updateNickname_conflict() throws Exception {
        // given
        userRepository.save(User.builder()
                .email("another@example.com")
                .username("dupeNick")
                .provider("KAKAO")
                .role(Role.USER)
                .state(State.ACTIVE)
                .build());

        UserUpdateRequestDto dto = new UserUpdateRequestDto("dupeNick");

        // when & then
        mockMvc.perform(patch("/users/me")
                        .header("Authorization", getAccessToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."));
    }
}
