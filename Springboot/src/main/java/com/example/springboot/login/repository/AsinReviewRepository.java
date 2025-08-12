package com.example.springboot.login.repository;

import com.example.springboot.login.entity.AsinReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsinReviewRepository extends JpaRepository<AsinReview, Long> {
    List<AsinReview> findByAsinIn(List<String> asins);
}