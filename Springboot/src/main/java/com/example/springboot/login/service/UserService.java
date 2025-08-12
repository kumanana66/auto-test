package com.example.springboot.login.service;

import com.example.springboot.login.entity.User;
import com.example.springboot.login.exception.AuthenticationException;

import java.time.LocalDateTime;
import java.util.Optional;

// 作为接口，定义用户相关业务方法的规范
public interface UserService {

    Optional<User> findByUsername(String username);

    void lockUser(String username, int failedAttempts);

    void unlockUser(String username);

    void resetFailedAttempts(String username);

    boolean isUserLocked(String username);

    boolean isUsernameExists(String username);

//    获取当前登录用户
    User getCurrentUser();
}