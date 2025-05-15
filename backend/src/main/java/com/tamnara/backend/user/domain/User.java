package com.tamnara.backend.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = {"email", "provider"}),
                @UniqueConstraint(name = "uk_user_provider_id", columnNames = {"provider", "providerId"}),
                @UniqueConstraint(name = "uk_user_username", columnNames = {"username"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 100) // password는 null 허용 (OAuth 로그인 사용자 대비)
    private String password;

    @Column(nullable = false, length = 10)
    private String username;

    @Column(nullable = false, length = 20)
    private String provider; // LOCAL, KAKAO

    @Column(length = 100)
    private String providerId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private State state;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime lastActiveAt;

    private LocalDateTime withdrawnAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt;
        this.updatedAt = this.updatedAt == null ? LocalDateTime.now() : this.updatedAt;
        this.lastActiveAt = this.lastActiveAt == null ? LocalDateTime.now() : this.lastActiveAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
    }

    public void updateLastActiveAtNow() {
        this.lastActiveAt = LocalDateTime.now();
    }
}
