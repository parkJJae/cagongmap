package com.mysite.cafe.domain.auth.entity;

import com.mysite.cafe.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 토큰 값 자체 (UUID 문자열). 조회 키
    @Column(nullable = false, unique = true, length = 100)
    private String token;

    // 누구의 토큰인지 (username)
    @Column(nullable = false, length = 50)
    private String username;

    // 만료 시각 (저장 시 now + RT 유효기간)
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private RefreshToken(String token, String username, LocalDateTime expiresAt) {
        this.token = token;
        this.username = username;
        this.expiresAt = expiresAt;
    }

    // 만료됐는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}