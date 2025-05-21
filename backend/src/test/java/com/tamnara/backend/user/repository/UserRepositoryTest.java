package com.tamnara.backend.user.repository;

import com.tamnara.backend.user.domain.Role;
import com.tamnara.backend.user.domain.State;
import com.tamnara.backend.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

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
    void existsByEmail_테스트() {
        User user = createTestUser();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("닉네임 중복 여부를 확인할 수 있다")
    void existsByUsername_테스트() {
        User user = createTestUser();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("탐라유저");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일과 provider로 유저를 조회할 수 있다")
    void findByEmailAndProvider_테스트() {
        User user = createTestUser();
        userRepository.save(user);

        Optional<User> result = userRepository.findByEmailAndProvider("test@example.com", "LOCAL");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("탐라유저");
    }

    @Test
    @DisplayName("provider와 providerId로 유저를 조회할 수 있다")
    void findByProviderAndProviderId_테스트() {
        User user = createTestUser();
        userRepository.save(user);

        Optional<User> result = userRepository.findByProviderAndProviderId("LOCAL", "12345");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }
}

