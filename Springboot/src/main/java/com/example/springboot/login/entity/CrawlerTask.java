package com.example.springboot.login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "crawler_task")
public class CrawlerTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String processName; // 流程名称

    @Column(nullable = false, length = 4096)
    private String asinList; // ASIN列表，逗号分隔

    @ElementCollection
    @CollectionTable(name = "crawler_task_required_info", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "info")
    private List<String> requiredInfo; // 所需信息列表

    @Column(nullable = false)
    private String platform; // 平台

    @Column(nullable = false)
    private String timeCycle; // 时间周期

    @Column(nullable = false)
    private LocalDateTime createTime; // 创建时间

    @Column
    private LocalDateTime updateTime; // 更新时间

    @Column
    private String status; // 任务状态：ACTIVE, DRAFT, PAUSED, COMPLETED

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 关联用户

}