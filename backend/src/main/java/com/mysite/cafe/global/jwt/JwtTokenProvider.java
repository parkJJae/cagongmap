package com.mysite.cafe.global.jwt;

import com.mysite.cafe.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    public JwtTokenProvider(JwtProperties properties) {
        this.secretKey = Keys.hmacShaKeyFor(
                properties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessTokenValiditySeconds = properties.getAccessTokenValiditySeconds();
        this.refreshTokenValiditySeconds = properties.getRefreshTokenValiditySeconds();
    }

    // Access Token 발급
    public String createAccessToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValiditySeconds * 1000);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 발급 (JWT 아님, 추측 불가능한 랜덤 문자열)
    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // RT 만료시간 (DB 저장 시 expiresAt 계산에 사용)
    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }

    // AT 유효성 검증 (서명,만료 둘 다)
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // AT에서 username 추출
    public String getUsernameFromAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}