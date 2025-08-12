package com.example.springboot.login.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Accessors(chain = true) // 让 setter 支持链式调用返回 this
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 6, max = 20)
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_locked", columnDefinition = "boolean default false")
    private boolean locked = false;

    @Column(name = "failed_attempts", columnDefinition = "int default 0")
    private int failedAttempts = 0;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // 新增头像字段
    @Column(name = "avatar")
    private String avatar; // 存储头像URL

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    // 链式调用方法
    public User setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
}