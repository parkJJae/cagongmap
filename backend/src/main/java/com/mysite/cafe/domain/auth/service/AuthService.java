package com.mysite.cafe.domain.auth.service;

import com.mysite.cafe.domain.admin.entity.Admin;
import com.mysite.cafe.domain.admin.repository.AdminRepository;
import com.mysite.cafe.domain.auth.dto.LoginRequest;
import com.mysite.cafe.domain.auth.dto.TokenResponse;
import com.mysite.cafe.domain.auth.entity.RefreshToken;
import com.mysite.cafe.domain.auth.repository.RefreshTokenRepository;
import com.mysite.cafe.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AdminRepository adminRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 로그인: username/password 검증 → AT + RT 발급
    @Transactional
    public TokenResponse login(LoginRequest request) {
        //username으로 admin 조회
        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "아이디 또는 비밀번호가 올바르지 않습니다."
                ));

        //비밀번호 검증
        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        //기존 RT 폐기 후 새로 발급
        refreshTokenRepository.deleteByUsername(admin.getUsername());

        //AT + RT 발급
        return issueTokens(admin.getUsername());
    }

    //RT 검증 → 기존 RT 폐기 + 새 AT/RT 발급 (Rotation)
    @Transactional
    public TokenResponse refresh(String refreshTokenValue) {
        // RT 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "유효하지 않은 토큰입니다."
                ));

        //만료 체크
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken); // 만료된 거 청소
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "만료된 토큰입니다. 다시 로그인해주세요."
            );
        }

        String username = refreshToken.getUsername();

        //기존 RT 폐기
        refreshTokenRepository.delete(refreshToken);

        // 새 AT + RT 발급
        return issueTokens(username);
    }

    // 로그아웃 해당 RT 폐기
    @Transactional
    public void logout(String refreshTokenValue) {
        // RT가 있으면 삭제, 없으면 그냥 넘어감 (이미 로그아웃된 상태로 간주)
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    // AT + RT 발급 + RT를 DB에 저장 (공통 로직)
    private TokenResponse issueTokens(String username) {
        String accessToken = jwtTokenProvider.createAccessToken(username);
        String refreshTokenValue = jwtTokenProvider.createRefreshToken();

        // RT를 DB에 저장
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenValiditySeconds());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .username(username)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenValue);
    }
}