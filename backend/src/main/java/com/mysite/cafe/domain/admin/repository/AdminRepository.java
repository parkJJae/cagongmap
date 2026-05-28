package com.mysite.cafe.domain.admin.repository;

import com.mysite.cafe.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 로그인 시 username으로 조회
    Optional<Admin> findByUsername(String username);
}