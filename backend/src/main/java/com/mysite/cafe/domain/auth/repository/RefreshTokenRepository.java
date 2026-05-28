package com.mysite.cafe.domain.auth.repository;

import com.mysite.cafe.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // 같은 username의 RT 모두 삭제 (RT Rotation 시 이전 거 폐기용)
    void deleteByUsername(String username);
}