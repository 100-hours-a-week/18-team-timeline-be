package com.tamnara.backend.utils;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;

public class CreateUserUtils {
    public static User createActiveUser(String email, String username, String provider, String providerId) {
        return User.builder()
                .email(email)
                .username(username)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .state(State.ACTIVE)
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
                .build();
    }
}
