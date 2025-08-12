package com.example.springboot.login.repository;

import com.example.springboot.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.locked = :locked, u.lockTime = :lockTime, u.failedAttempts = :failedAttempts " +
            "WHERE u.username = :username")
    int updateUserLockStatus(@Param("username") String username,
                             @Param("locked") boolean locked,
                             @Param("lockTime") LocalDateTime lockTime,
                             @Param("failedAttempts") int failedAttempts);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0 WHERE u.username = :username")
    int resetFailedAttempts(@Param("username") String username);
}