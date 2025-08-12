package com.example.springboot.login.repository;

import com.example.springboot.login.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    //删除所有过期的验证码
    @Modifying
    @Transactional
    @Query("DELETE FROM VerificationCode vc WHERE vc.expireTime < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    List<VerificationCode> findByEmailAndExpireTimeAfterOrderByCreateTimeDesc(String email, LocalDateTime now);
}