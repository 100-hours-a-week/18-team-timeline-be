package com.tamnara.backend.user.service;

import com.tamnara.backend.global.constant.JwtConstant;
import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.global.exception.CustomException;
import com.tamnara.backend.global.jwt.JwtProvider;
import com.tamnara.backend.user.constant.UserResponseMessage;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.dto.SignupRequest;
import com.tamnara.backend.user.dto.SignupResponse;
import com.tamnara.backend.user.dto.UserInfo;
import com.tamnara.backend.user.dto.UserWithdrawInfo;
import com.tamnara.backend.user.exception.DuplicateEmailException;
import com.tamnara.backend.user.exception.InactiveUserException;
import com.tamnara.backend.user.exception.UserNotFoundException;
import com.tamnara.backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    public SignupResponse signup(SignupRequest requestDto) {
        log.info("[USER] signup 시작");
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            log.warn("[USER] signup 예외 처리 - state:{}", "이메일 중복");
            throw new DuplicateEmailException();
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
        log.info("[USER] signup 완료 - userId={}", savedUser.getId());

        return SignupResponse.builder()
                .success(true)
                .message(UserResponseMessage.REGISTER_SUCCESSFUL)
                .data(SignupResponse.UserData.builder()
                        .userId(savedUser.getId())
                        .build())
                .build();
    }

    @Override
    public boolean isEmailAvailable(String email) {
        log.info("[USER] isEmailAvailable 시작");

        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.warn("[USER] isEmailAvailable 예외 처리 - state:{}", "이메일 형식 오류");
            throw new CustomException(HttpStatus.BAD_REQUEST, UserResponseMessage.EMAIL_BAD_REQUEST);
        }

        boolean result = !userRepository.existsByEmail(email);
        log.info("[USER] isEmailAvailable 완료 - available: {}", result);
        return result;
    }

    @Override
    public UserInfo getCurrentUserInfo(Long userId) {
        log.info("[USER] getCurrentUserInfo 시작 - userId: {}", userId);

        User user = checkValidateUser(userId);

        log.info("[USER] getCurrentUserInfo 완료 - userId: {}", userId);
        return new UserInfo(user.getId(), user.getEmail(), user.getUsername());
    }

    @Override
    @Transactional
    public User updateUsername(Long userId, String newUsername) {
        log.info("[USER] updateUsername 시작 - userId: {}", userId);

        User user = checkValidateUser(userId);
        user.updateUsername(newUsername);
        log.info("[USER] updateUsername 완료 - userId: {}", userId);

        return user;
    }

    @Override
    public UserWithdrawInfo withdrawUser(Long userId, HttpServletResponse response) {
        log.info("[USER] withdrawUser 시작 - userId: {}", userId);

        User user = checkValidateUser(userId);

        jwtProvider.deleteRefreshToken(userId);
        log.info("[USER] withdrawUser 처리 중 - Redis에서 refresh token 제거, userId: {}", userId);

        expireCookies(userId, response);
        log.info("[USER] withdrawUser 처리 중 - access token, refresh token 저장 쿠키 초기화, userId: {}", userId);

        user.softDelete();
        log.info("[USER] withdrawUser 완료 - userId: {}, withdrawnAt: {}", user.getId(), user.getWithdrawnAt());

        return new UserWithdrawInfo(user.getId(), user.getWithdrawnAt());
    }

    @Override
    public void logout(Long userId, HttpServletResponse response) {
        log.info("[USER] logout 시작 - userId: {}", userId);

        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        log.info("[USER] logout 처리 중 - 회원 조회 성공, userId: {}", userId);

        jwtProvider.deleteRefreshToken(userId);
        log.info("[USER] logout 처리 중 - Redis에서 refresh token 제거, userId: {}", userId);

        expireCookies(userId, response);
        log.info("[USER] logout 처리 중 - 응답의 쿠키 초기화, userId: {}", userId);

        log.info("[USER] logout 완료 - userId: {}", userId);
    }


    /**
     * 헬퍼 메서드
     */

    private User checkValidateUser (Long userId) {
        log.info("[USER] checkValidateUser 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ResponseMessage.USER_NOT_FOUND));
        log.info("[USER] checkValidateUser 처리 중 - 회원 조회 성공, userId: {}", userId);

        if (user.getState() != State.ACTIVE) {
            log.warn("[USER] checkValidateUser 예외 처리 - 비활성 회원 검증, userId: {}, state: {}", user.getId(), user.getState());
            throw new InactiveUserException();
        }
        log.info("[USER] checkValidateUser 처리 중 - 활성 회원 검증, userId: {}, state: {}", userId, user.getState());

        log.info("[USER] checkValidateUser 완료 - userId: {}", userId);
        return user;
    }

    private void expireCookies(Long userId, HttpServletResponse response) {
        log.info("[USER] expireCookies 시작 - userId: {}", userId);

        Cookie expiredAccessCookie = new Cookie(JwtConstant.ACCESS_TOKEN, null);
        expiredAccessCookie.setHttpOnly(true);
        expiredAccessCookie.setSecure(true);
        expiredAccessCookie.setPath("/");
        expiredAccessCookie.setMaxAge(0);
        response.addCookie(expiredAccessCookie);
        log.info("[USER] expireCookies 처리 중 - access token 저장 쿠키 초기화, userId: {}", userId);

        Cookie expiredRefreshCookie = new Cookie(JwtConstant.REFRESH_TOKEN, null);
        expiredRefreshCookie.setHttpOnly(true);
        expiredRefreshCookie.setSecure(true);
        expiredRefreshCookie.setPath("/");
        expiredRefreshCookie.setMaxAge(0);
        response.addCookie(expiredRefreshCookie);
        log.info("[USER] expireCookies 처리 중 - refresh token 저장 쿠키 초기화, userId: {}", userId);

        log.info("[USER] expireCookies 완료 - userId: {}", userId);
    }
}
