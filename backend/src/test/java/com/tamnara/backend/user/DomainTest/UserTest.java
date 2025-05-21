package com.tamnara.backend.user.DomainTest;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
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
    void username을_변경할_수_있다() {
        // given
        String newUsername = "새로운탐라";

        // when
        user.updateUsername(newUsername);

        // then
        assertThat(user.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void 마지막_활동시간을_지금으로_업데이트할_수_있다() {
        // given
        user.prePersist();  // 생성 시간 초기화
        LocalDateTime beforeUpdate = user.getLastActiveAt();

        // when
        user.updateLastActiveAtNow();

        // then
        assertThat(user.getLastActiveAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    void prePersist_호출시_생성시간_갱신된다() {
        // when
        user.prePersist();

        // then
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getLastActiveAt()).isNotNull();
    }

    @Test
    void preUpdate_호출시_수정시간이_갱신된다() throws InterruptedException {
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
