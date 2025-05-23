package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.UserInfoResponseDto;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이메일 사용 가능 여부를 확인한다")
    void isEmailAvailable_success() {
        // given
        Mockito.when(userRepository.existsByEmail("a@a.com")).thenReturn(false);

        // when, then
        assertThat(userService.isEmailAvailable("a@a.com")).isTrue();
    }

    @Test
    @DisplayName("닉네임 사용 가능 여부를 확인한다")
    void isUsernameAvailable_success() {
        // given
        Mockito.when(userRepository.existsByUsername("탐라")).thenReturn(true);

        // when, then
        assertThat(userService.isUsernameAvailable("탐라")).isFalse();
    }

    @Test
    @DisplayName("회원 정보 조회에 성공한다")
    void getCurrentUserInfo_success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("탐라")
                .state(State.ACTIVE)
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserInfoResponseDto response = userService.getCurrentUserInfo(1L);

        // then
        assertThat(response.getData().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("비활성화된 사용자는 조회할 수 없다")
    void getCurrentUserInfo_inactiveUser_throwsException() {
        // given
        User user = User.builder()
                .id(1L)
                .state(State.DELETED)
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> userService.getCurrentUserInfo(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("닉네임 변경에 성공한다")
    void updateUsername_success() {
        // given
        User user = User.builder()
                .id(1L)
                .username("old")
                .state(State.ACTIVE)
                .build();
        Mockito.when(userRepository.existsByUsername("new")).thenReturn(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        User result = userService.updateUsername(1L, "new");

        // then
        assertThat(result.getUsername()).isEqualTo("new");
    }

    @Test
    @DisplayName("닉네임이 중복되면 변경할 수 없다")
    void updateUsername_duplicate_throwsException_success() {
        // given
        Mockito.when(userRepository.existsByUsername("existing")).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> userService.updateUsername(1L, "existing"))
                .isInstanceOf(DuplicateUsernameException.class);
    }
}
