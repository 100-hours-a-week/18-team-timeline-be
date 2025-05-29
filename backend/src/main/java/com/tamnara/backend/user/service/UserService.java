package com.tamnara.backend.user.service;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequest;
import com.tamnara.backend.user.dto.SignupResponse;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.exception.DuplicateEmailException;
import com.tamnara.backend.user.exception.DuplicateUsernameException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.tamnara.backend.user.constant.UserResponseMessage.REGISTER_SUCCESSFUL;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateEmailException();
        }
        if (userRepository.existsByUsername(requestDto.getUsername())) {
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

        return SignupResponse.builder()
                .success(true)
                .message(REGISTER_SUCCESSFUL)
                .data(SignupResponse.UserData.builder()
                        .userId(savedUser.getId())
                        .build())
                .build();
    }

    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    public UserInfo getCurrentUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (user.getState() != State.ACTIVE) {
            throw new UserNotFoundException(); // DELETED나 INACTIVE는 허용하지 않음
        }

        return new UserInfo(user.getId(), user.getEmail(), user.getUsername());
    }

    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        // 닉네임 중복 검사
        if (userRepository.existsByUsername(newUsername)) {
            throw new DuplicateUsernameException();
        }

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        user.updateUsername(newUsername); // 엔티티에 닉네임 변경 메서드 필요

        return user;
    }
}
