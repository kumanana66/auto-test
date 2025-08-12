package com.example.springboot.login.repository;

import com.example.springboot.login.entity.CrawlerTask;
import com.example.springboot.login.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlerTaskRepository
        extends JpaRepository<CrawlerTask, Long>,
        JpaSpecificationExecutor<CrawlerTask> {

    List<CrawlerTask> findByUser(User user);
    List<CrawlerTask> findByStatus(String status);
//    Page<CrawlerTask> findByFilters(String status, String timeCycle, String platform, String keyword, Pageable pageable);
}