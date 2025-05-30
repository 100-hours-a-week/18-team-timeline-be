package com.tamnara.backend.user.security;

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
        log.info("loadUserByUsername 진입: userIdStr={}", userIdStr);

        try {
            Long userId = Long.parseLong(userIdStr);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("사용자 조회 실패: userId={}", userId);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
                    });

            log.info("사용자 조회 성공: userId={}", userId);
            return new UserDetailsImpl(user);

        } catch (NumberFormatException e) {
            log.error("userId 파싱 실패: userIdStr={}", userIdStr, e);
            throw new UsernameNotFoundException("잘못된 사용자 ID 형식입니다.");
        }
    }
}
