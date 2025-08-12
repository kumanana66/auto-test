package com.example.springboot.login.dto;

import com.example.springboot.login.entity.AsinReview;
import com.example.springboot.login.entity.CrawlerTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsinReviewDTO {

    private Long id;
    private String asin;
    private String brand;
    private String reviewId;
    private String reviewerName;
    private String reviewTitle;
    private String reviewContent;
    private Double reviewRating;
    private String reviewDate;
    private Integer helpfulVotes;
    private String images;
    private LocalDateTime crawlTime;
    private Long taskId; // 任务ID

    // 从实体类转换为DTO
    public static AsinReviewDTO fromEntity(AsinReview entity) {
        AsinReviewDTO dto = new AsinReviewDTO();
        dto.setId(entity.getId());
        dto.setAsin(entity.getAsin());
        dto.setBrand(entity.getBrand());
        dto.setReviewId(entity.getReviewId());
        dto.setReviewerName(entity.getReviewerName());
        dto.setReviewTitle(entity.getReviewTitle());
        dto.setReviewContent(entity.getReviewContent());
        dto.setReviewRating(entity.getReviewRating());
        dto.setReviewDate(entity.getReviewDate());
        dto.setHelpfulVotes(entity.getHelpfulVotes());
        dto.setImages(entity.getImages());
        dto.setCrawlTime(entity.getCrawlTime());

        // 提取任务ID
        if (entity.getTask() != null) {
            dto.setTaskId(entity.getTask().getId());
        }

        return dto;
    }

    // 从DTO转换为实体类（包含任务关联）
    public AsinReview toEntity(CrawlerTask task) {
        AsinReview entity = new AsinReview();
        entity.setId(this.getId());
        entity.setAsin(this.getAsin());
        entity.setBrand(this.getBrand());
        entity.setReviewId(this.getReviewId());
        entity.setReviewerName(this.getReviewerName());
        entity.setReviewTitle(this.getReviewTitle());
        entity.setReviewContent(this.getReviewContent());
        entity.setReviewRating(this.getReviewRating());
        entity.setReviewDate(this.getReviewDate());
        entity.setHelpfulVotes(this.getHelpfulVotes());
        entity.setImages(this.getImages());
        entity.setCrawlTime(this.getCrawlTime());

        // 设置任务关联
        entity.setTask(task);

        return entity;
    }
}