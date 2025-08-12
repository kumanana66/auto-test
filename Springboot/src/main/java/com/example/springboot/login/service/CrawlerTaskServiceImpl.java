package com.example.springboot.login.service;

import com.example.springboot.login.entity.AsinPriceRank;
import com.example.springboot.login.entity.AsinReview;
import com.example.springboot.login.entity.CrawlerTask;
import com.example.springboot.login.entity.User;
import com.example.springboot.login.repository.AsinPriceRankRepository;
import com.example.springboot.login.repository.AsinReviewRepository;
import com.example.springboot.login.repository.CrawlerTaskRepository;
import com.example.springboot.login.service.CrawlerTaskService;
import com.example.springboot.login.specification.CrawlerTaskSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CrawlerTaskServiceImpl implements CrawlerTaskService {

    @Autowired
    private CrawlerTaskRepository crawlerTaskRepository;

    @Autowired
    private AsinPriceRankRepository priceRankRepository;

    @Autowired
    private AsinReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Override
    public CrawlerTask createTask(CrawlerTask task) {
        // 验证ASIN列表格式
        String asinList = task.getAsinList();
        if (asinList.contains(" ") || !asinList.matches("^[A-Z0-9,]+$")) {
            throw new IllegalArgumentException("ASIN列表格式错误，请使用逗号分隔");
        }

        // 1. 自动设置创建时间和更新时间为当前时间
        LocalDateTime now = LocalDateTime.now();
        task.setCreateTime(now);
        task.setUpdateTime(now);

        // 2. 设置默认状态为 ACTIVE（如果前端未指定）
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("ACTIVE");
        }

        // 3. 关联当前登录用户
        User currentUser = userService.getCurrentUser();
        task.setUser(currentUser);

        return crawlerTaskRepository.save(task);
    }

    @Override
    public CrawlerTask saveAsDraft(CrawlerTask task) {
        task.setStatus("DRAFT");
        return crawlerTaskRepository.save(task);
    }

    @Override
    public Page<CrawlerTask> getUserTasksWithFilters(String status, String timeCycle, String platform, String keyword, Pageable pageable) {
        // 这里正确写法是用 Specification 组合条件
        Specification<CrawlerTask> spec = Specification
                .where(CrawlerTaskSpecifications.byStatus(status))
                .and(CrawlerTaskSpecifications.byTimeCycle(timeCycle))
                .and(CrawlerTaskSpecifications.byPlatform(platform))
                .and(CrawlerTaskSpecifications.byKeyword(keyword));
        return crawlerTaskRepository.findAll(spec, pageable);
    }

    @Override
    public CrawlerTask getTaskById(Long id) {
        return crawlerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Override
    public CrawlerTask updateTask(Long id, CrawlerTask taskDetails) {
        CrawlerTask task = crawlerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        task.setProcessName(taskDetails.getProcessName());
        task.setAsinList(taskDetails.getAsinList());
        task.setRequiredInfo(taskDetails.getRequiredInfo());
        task.setPlatform(taskDetails.getPlatform());
        task.setTimeCycle(taskDetails.getTimeCycle());
        task.setStatus(taskDetails.getStatus());

        return crawlerTaskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id) {
        CrawlerTask task = crawlerTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        crawlerTaskRepository.delete(task);
    }

    @Override
    public List<AsinPriceRank> getAsinPriceRanksByTaskId(Long taskId) {
        CrawlerTask task = crawlerTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

//        // 解析ASIN列表
//        String[] asins = task.getAsinList().split(",");
//
//        // 查询最新的价格数据（每个ASIN取最近一条记录）
//        return priceRankRepository.findLatestByAsins(Arrays.asList(asins));
        // 直接查询该任务下的所有价格数据，无需解析ASIN列表
        return priceRankRepository.findByTaskId(taskId);
    }

    @Override
    public List<AsinReview> getAsinReviewsByTaskId(Long taskId) {
        CrawlerTask task = crawlerTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // 解析ASIN列表
        String[] asins = task.getAsinList().split(",");

        // 查询所有评论数据
        return reviewRepository.findByAsinIn(Arrays.asList(asins));
    }
}