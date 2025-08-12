package com.example.springboot.login.service;

import com.example.springboot.login.entity.AsinPriceRank;
import com.example.springboot.login.entity.AsinReview;
import com.example.springboot.login.entity.CrawlerTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrawlerTaskService {

    // 任务基本操作
    CrawlerTask createTask(CrawlerTask task);
    CrawlerTask saveAsDraft(CrawlerTask task);
    Page<CrawlerTask> getUserTasksWithFilters(String status, String timeCycle, String platform, String keyword, Pageable pageable);
    CrawlerTask getTaskById(Long id);
    CrawlerTask updateTask(Long id, CrawlerTask taskDetails);
    void deleteTask(Long id);

    // ASIN价格数据操作
    List<AsinPriceRank> getAsinPriceRanksByTaskId(Long taskId);

    // ASIN评论数据操作
    List<AsinReview> getAsinReviewsByTaskId(Long taskId);
}