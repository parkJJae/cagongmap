package com.mysite.cafe.domain.admin;

import com.mysite.cafe.domain.admin.entity.Admin;
import com.mysite.cafe.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        // 이미 있으면 skip (멱등성)
        if (adminRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Admin '{}' already exists. skip seeding.", adminUsername);
            return;
        }

        Admin admin = Admin.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .build();
        adminRepository.save(admin);
        log.info("Admin '{}' seeded.", adminUsername);
    }
}