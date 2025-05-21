package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequestDto;
import com.tamnara.backend.user.dto.SignupResponseDto;
import com.tamnara.backend.user.dto.UserInfoResponseDto;
import com.tamnara.backend.user.exception.DuplicateEmailException;
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

//    @Test
//    @DisplayName("이메일과 닉네임이 중복되지 않으면 회원가입에 성공한다")
//    void signup_success() {
//        // given
//        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "Password1234!", "탐라유저");
//        Mockito.when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
//        Mockito.when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(false);
//        Mockito.when(passwordEncoder.encode(requestDto.getPassword())).thenReturn("hashedPassword");
//
//        User mockUser = User.builder()
//                .id(1L)
//                .email(requestDto.getEmail())
//                .username(requestDto.getUsername())
//                .password("hashedPassword")
//                .role(Role.USER)
//                .state(State.ACTIVE)
//                .provider("LOCAL")
//                .build();
//
//        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenReturn(mockUser);
//
//        // when
//        SignupResponseDto response = userService.signup(requestDto);
//
//        // then
//        assertThat(response.isSuccess()).isTrue();
//        assertThat(response.getData().getUserId()).isEqualTo(1L);
//    }
//
//    @Test
//    @DisplayName("이메일이 중복되면 예외가 발생한다")
//    void signup_duplicateEmail_throwsException() {
//        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "pass", "user");
//        Mockito.when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(true);
//
//        assertThatThrownBy(() -> userService.signup(requestDto))
//                .isInstanceOf(DuplicateEmailException.class);
//    }
//
//    @Test
//    @DisplayName("닉네임이 중복되면 예외가 발생한다")
//    void signup_duplicateUsername_throwsException() {
//        SignupRequestDto requestDto = new SignupRequestDto("test@example.com", "pass", "user");
//        Mockito.when(userRepository.existsByEmail(requestDto.getEmail())).thenReturn(false);
//        Mockito.when(userRepository.existsByUsername(requestDto.getUsername())).thenReturn(true);
//
//        assertThatThrownBy(() -> userService.signup(requestDto))
//                .isInstanceOf(DuplicateUsernameException.class);
//    }

    @Test
    @DisplayName("이메일 사용 가능 여부를 확인한다")
    void isEmailAvailable_테스트() {
        Mockito.when(userRepository.existsByEmail("a@a.com")).thenReturn(false);

        assertThat(userService.isEmailAvailable("a@a.com")).isTrue();
    }

    @Test
    @DisplayName("닉네임 사용 가능 여부를 확인한다")
    void isUsernameAvailable_테스트() {
        Mockito.when(userRepository.existsByUsername("탐라")).thenReturn(true);

        assertThat(userService.isUsernameAvailable("탐라")).isFalse();
    }

    @Test
    @DisplayName("회원 정보 조회에 성공한다")
    void getCurrentUserInfo_테스트() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("탐라")
                .state(State.ACTIVE)
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserInfoResponseDto response = userService.getCurrentUserInfo(1L);

        assertThat(response.getData().getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("비활성화된 사용자는 조회할 수 없다")
    void getCurrentUserInfo_inactiveUser_throwsException_테스트() {
        User user = User.builder()
                .id(1L)
                .state(State.DELETED)
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.getCurrentUserInfo(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("닉네임 변경에 성공한다")
    void updateUsername_테스트() {
        User user = User.builder()
                .id(1L)
                .username("old")
                .state(State.ACTIVE)
                .build();

        Mockito.when(userRepository.existsByUsername("new")).thenReturn(false);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.updateUsername(1L, "new");

        assertThat(result.getUsername()).isEqualTo("new");
    }

    @Test
    @DisplayName("닉네임이 중복되면 변경할 수 없다")
    void updateUsername_duplicate_throwsException_테스트() {
        Mockito.when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUsername(1L, "existing"))
                .isInstanceOf(DuplicateUsernameException.class);
    }
}
