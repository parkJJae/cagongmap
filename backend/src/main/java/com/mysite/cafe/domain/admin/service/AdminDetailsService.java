package com.mysite.cafe.domain.admin.service;

import com.mysite.cafe.domain.admin.entity.Admin;
import com.mysite.cafe.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "관리자를 찾을 수 없습니다: " + username
                ));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())  // BCrypt 해시 그대로 전달
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
    }
}