package com.tamnara.backend.global.util;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;

import java.time.LocalDateTime;

public class CreateUserUtil {
    public static User createActiveUser(String email, String username, String provider, String providerId) {
        return User.builder()
                .email(email)
                .username(username)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .state(State.ACTIVE)
                .lastActiveAt(LocalDateTime.now())
                .build();
    }

    public static User createDeletedUser(String email, String username, String provider, String providerId) {
        return User.builder()
                .email(email)
                .username(username)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .state(State.DELETED)
                .lastActiveAt(LocalDateTime.now())
                .build();
    }
}
