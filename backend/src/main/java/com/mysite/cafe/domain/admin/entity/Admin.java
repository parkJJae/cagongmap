package com.mysite.cafe.domain.admin.entity;

import com.mysite.cafe.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // BCrypt 해시 저장 (평문 절대 X)
    @Column(nullable = false, length = 100)
    private String password;

    @Builder
    private Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }
}