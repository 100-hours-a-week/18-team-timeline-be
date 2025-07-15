package com.tamnara.backend.user.security;

import com.tamnara.backend.global.constant.ResponseMessage;
import com.tamnara.backend.user.constant.UserResponseMessage;
import com.tamnara.backend.user.domain.User;
import com.tamnara.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String userIdStr) throws UsernameNotFoundException {
        log.info("[USER] loadUserByUsername 시작 - userIdStr:{}", userIdStr);

        try {
            Long userId = Long.parseLong(userIdStr);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("[NEWS] loadUserByUsername 처리 중 - 사용자 조회 실패, userId:{}", userId);
                        return new UsernameNotFoundException(ResponseMessage.USER_NOT_FOUND);
                    });
            log.info("[NEWS] loadUserByUsername 처리 중 - 사용자 조회 성공, userId:{}", userId);

            return new UserDetailsImpl(user);
        } catch (NumberFormatException e) {
            log.error("[NEWS] loadUserByUsername 실패 - userId 파싱 실패, userIdStr:{}", userIdStr);
            throw new UsernameNotFoundException(UserResponseMessage.ID_UNAVAILABLE);
        }
    }
}
