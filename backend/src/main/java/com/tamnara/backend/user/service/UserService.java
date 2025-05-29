package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.*;
import com.tamnara.backend.user.exception.DuplicateEmailException;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.tamnara.backend.user.constant.UserResponseMessage.REGISTER_SUCCESSFUL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest requestDto) {
        log.info("signup 진입: email={}, username={}", requestDto.getEmail(), requestDto.getUsername());
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            log.warn("이메일 중복: {}", requestDto.getEmail());
            throw new DuplicateEmailException();
        }
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            log.warn("닉네임 중복: {}", requestDto.getUsername());
            throw new DuplicateUsernameException();
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .username(requestDto.getUsername())
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 성공: userId={}", savedUser.getId());

        return SignupResponse.builder()
                .success(true)
                .message(REGISTER_SUCCESSFUL)
                .data(SignupResponse.UserData.builder()
                        .userId(savedUser.getId())
                        .build())
                .build();
    }

    public boolean isEmailAvailable(String email) {
        boolean result = !userRepository.existsByEmail(email);
        log.info("이메일 사용 가능 여부: email={}, available={}", email, result);
        return result;
    }

    public boolean isUsernameAvailable(String username) {
        boolean result = !userRepository.existsByUsername(username);
        log.info("닉네임 사용 가능 여부: username={}, available={}", username, result);
        return result;
    }

    public UserInfo getCurrentUserInfo(Long userId) {
        log.info("getCurrentUserInfo 진입: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원 조회 실패: userId={}", userId);
                    return new UserNotFoundException();
                });

        if (user.getState() != State.ACTIVE) {
            log.warn("비활성 회원 접근 차단: userId={}, state={}", user.getId(), user.getState());
            throw new UserNotFoundException(); // DELETED나 INACTIVE는 허용하지 않음
        }

        return new UserInfo(user.getId(), user.getEmail(), user.getUsername());
    }

    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        log.info("updateUsername 진입: userId={}, newUsername={}", userId, newUsername);

        if (userRepository.existsByUsername(newUsername)) {
            log.warn("닉네임 중복: {}", newUsername);
            throw new DuplicateUsernameException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원 조회 실패: userId={}", userId);
                    return new UserNotFoundException();
                });

        user.updateUsername(newUsername);
        log.info("닉네임 변경 성공: userId={}, updatedUsername={}", userId, newUsername);

        return user;
    }

    public UserWithdrawInfoWrapper withdrawUser(Long userId) {
        log.info("withdrawUser 진입: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원 조회 실패: userId={}", userId);
                    return new UserNotFoundException();
                });

        user.softDelete();
        log.info("회원 탈퇴 처리 완료: userId={}, withdrawnAt={}", user.getId(), user.getWithdrawnAt());

        return new UserWithdrawInfoWrapper(
                new UserWithdrawInfo(user.getId(), user.getWithdrawnAt())
        );
    }
}
