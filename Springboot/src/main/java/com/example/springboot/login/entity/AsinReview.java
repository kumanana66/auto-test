package com.example.springboot.login.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asin_review")
public class AsinReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asin;
    private String brand; // 品牌
    private String reviewId;
    private String reviewerName;
    private String reviewTitle;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reviewContent;

    private Double reviewRating;
    private String reviewDate;
    private Integer helpfulVotes;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String images;

    private LocalDateTime crawlTime;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private CrawlerTask task;
}