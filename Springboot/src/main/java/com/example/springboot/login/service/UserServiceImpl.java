package com.example.springboot.login.service;

import com.example.springboot.login.entity.User;
import com.example.springboot.login.exception.AuthenticationException;
import com.example.springboot.login.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

// 标注 @Service，让 Spring 扫描识别为 Bean，注入到需要的地方
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public void lockUser(String username, int failedAttempts) {
        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(30);
        int updated = userRepository.updateUserLockStatus(
                username, true, lockTime, failedAttempts
        );

        if (updated == 0) {
            throw new AuthenticationException("用户不存在");
        }
    }

    @Override
    public void unlockUser(String username) {
        int updated = userRepository.updateUserLockStatus(
                username, false, null, 0
        );

        if (updated == 0) {
            throw new AuthenticationException("用户不存在");
        }
    }

    @Override
    public void resetFailedAttempts(String username) {
        int updated = userRepository.resetFailedAttempts(username);
        if (updated == 0) {
            throw new AuthenticationException("用户不存在");
        }
    }

    @Override
    public boolean isUserLocked(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.map(User::isLocked).orElse(false);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("未认证的用户");
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("用户不存在"));
    }
}