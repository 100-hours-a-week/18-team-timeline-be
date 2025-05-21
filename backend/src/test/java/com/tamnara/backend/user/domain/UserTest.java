package com.tamnara.backend.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .password("Password1234@")
                .username("탐라유저")
                .provider("LOCAL")
                .providerId(null)
                .role(Role.USER)
                .state(State.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("username을 업데이트할 수 있다")
    void updateUsername_테스트() {
        // given
        String newUsername = "새로운탐라";

        // when
        user.updateUsername(newUsername);

        // then
        assertThat(user.getUsername()).isEqualTo(newUsername);
    }

    @Test
    @DisplayName("마지막 활동시간을 지금으로 업데이트할 수 있다")
    void updateLastActiveAtNow_테스트() {
        // given
        user.prePersist();  // 생성 시간 초기화
        LocalDateTime beforeUpdate = user.getLastActiveAt();

        // when
        user.updateLastActiveAtNow();

        // then
        assertThat(user.getLastActiveAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("prePersist 호출 시 생성 시각, 업데이트 시각, 마지막 활동 시각이 갱신된다")
    void call_prePersist_테스트() {
        // when
        user.prePersist();

        // then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getLastActiveAt()).isNotNull();
    }

    @Test
    @DisplayName("preUpdate 호출 시 업데이트 시각이 갱신된다")
    void call_preUpdate_테스트() throws InterruptedException {
        // given
        user.prePersist(); // 초기화
        LocalDateTime beforeUpdate = user.getUpdatedAt();

        Thread.sleep(10); // 시간 차이 주기

        // when
        user.preUpdate();

        // then
        assertThat(user.getUpdatedAt()).isAfter(beforeUpdate);
    }
}
