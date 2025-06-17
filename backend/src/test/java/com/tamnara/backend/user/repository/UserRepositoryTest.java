package com.tamnara.backend.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @MockBean
    private JPAQueryFactory jpaQueryFactory;

    @Autowired
    private UserRepository userRepository;

    private User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .password("Password1234@")
                .username("탐라유저")
                .provider("LOCAL")
                .providerId("12345")
                .role(Role.USER)
                .state(State.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastActiveAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("이메일 중복 여부를 확인할 수 있다")
    void existsByEmail_success() {
        // given
        User user = createTestUser();
        userRepository.save(user);

        //when
        boolean exists = userRepository.existsByEmail("test@example.com");

        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 여부를 확인할 수 있다")
    void existsByUsername_success() {
        // given
        User user = createTestUser();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername("탐라유저");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일과 provider로 유저를 조회할 수 있다")
    void findByEmailAndProvider_success() {
        // given
        User user = createTestUser();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmailAndProvider("test@example.com", "LOCAL");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("탐라유저");
    }

    @Test
    @DisplayName("provider와 providerId로 유저를 조회할 수 있다")
    void findByProviderAndProviderId_success() {
        // given
        User user = createTestUser();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByProviderAndProviderId("LOCAL", "12345");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
}

