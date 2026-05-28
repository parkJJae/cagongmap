package com.mysite.cafe.domain.auth.controller;

import com.mysite.cafe.domain.auth.dto.LoginRequest;
import com.mysite.cafe.domain.auth.dto.LogoutRequest;
import com.mysite.cafe.domain.auth.dto.RefreshRequest;
import com.mysite.cafe.domain.auth.dto.TokenResponse;
import com.mysite.cafe.domain.auth.service.AuthService;
import com.mysite.cafe.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody LoginRequest request
    ) {
        TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @RequestBody RefreshRequest request
    ) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody LogoutRequest request
    ) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success());
    }
}