package com.tamnara.backend.user.service;

import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.dto.UserWithdrawInfo;
import com.tamnara.backend.user.exception.InactiveUserException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks private UserServiceImpl userService;
    @Mock private UserRepository userRepository;
    @Mock private JwtProvider jwtProvider;

    @Test
    @DisplayName("이메일 사용 가능 여부를 확인한다")
    void isEmailAvailable_success() {
        // given
        Mockito.when(userRepository.existsByEmail("a@a.com")).thenReturn(false);

        // when, then
        assertThat(userService.isEmailAvailable("a@a.com")).isTrue();
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
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
        UserInfo data = userService.getCurrentUserInfo(1L);

        // then
        assertThat(data.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("비활성화된 사용자 조회 불가")
    void getCurrentUserInfo_inactiveUser_throwsException() {
        // given
        User user = User.builder()
                .id(1L)
                .state(State.DELETED)
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> userService.getCurrentUserInfo(1L))
                .isInstanceOf(InactiveUserException.class);
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void updateUsername_success() {
        // given
        User user = User.builder()
                .id(1L)
                .username("old")
                .state(State.ACTIVE)
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        User result = userService.updateUsername(1L, "new");

        // then
        assertThat(result.getUsername()).isEqualTo("new");
    }

    @Test
    @DisplayName("회원탈퇴 시 Soft Delete 처리 성공")
    void withdrawUser_success() {
        // given
        User user = User.builder()
                .id(1L)
                .state(State.ACTIVE)
                .build();
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        UserWithdrawInfo userWithdrawInfo = userService.withdrawUser(1L, response);

        // then
        assertThat(user.getState()).isEqualTo(State.DELETED);
        assertThat(user.getWithdrawnAt()).isNotNull();
        assertThat(user.getWithdrawnAt()).isBeforeOrEqualTo(LocalDateTime.now());

        assertThat(userWithdrawInfo.userId()).isEqualTo(1L);
        assertThat(userWithdrawInfo.withdrawnAt()).isEqualTo(user.getWithdrawnAt());
    }

    @Test
    @DisplayName("존재하지 않는 회원탈퇴 시 예외 발생")
    void withdrawUser_userNotFound_throwsException() {
        // given
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when, then
        assertThatThrownBy(() -> userService.withdrawUser(999L, response))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ResponseMessage.USER_NOT_FOUND);
    }
}
