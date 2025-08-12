package com.example.springboot.login.dto;

import com.example.springboot.login.entity.AsinPriceRank;
import com.example.springboot.login.entity.CrawlerTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsinPriceRankDTO {

    private Long id;
    private String asin;
    private String brand;
    private Double originalPrice;
    private Double ldDiscount;
    private Double bdDiscount;
    private Double memberPrice;
    private Double memberFinalPrice;
    private Double nonMemberFinalPrice;
    private Double coupon;
    private Double directDiscount;
    private String mainCategory;
    private Integer mainCategoryRank;
    private String subCategory;
    private Integer subCategoryRank;
    private LocalDateTime crawlTime;
    private Long taskId; // 任务ID

    // 从实体类转换为DTO
    public static AsinPriceRankDTO fromEntity(AsinPriceRank entity) {
        AsinPriceRankDTO dto = new AsinPriceRankDTO();
        dto.setId(entity.getId());
        dto.setAsin(entity.getAsin());
        dto.setBrand(entity.getBrand());
        dto.setOriginalPrice(entity.getOriginalPrice());
        dto.setLdDiscount(entity.getLdDiscount());
        dto.setBdDiscount(entity.getBdDiscount());
        dto.setMemberPrice(entity.getMemberPrice());
        dto.setMemberFinalPrice(entity.getMemberFinalPrice());
        dto.setNonMemberFinalPrice(entity.getNonMemberFinalPrice());
        dto.setCoupon(entity.getCoupon());
        dto.setDirectDiscount(entity.getDirectDiscount());
        dto.setMainCategory(entity.getMainCategory());
        dto.setMainCategoryRank(entity.getMainCategoryRank());
        dto.setSubCategory(entity.getSubCategory());
        dto.setSubCategoryRank(entity.getSubCategoryRank());
        dto.setCrawlTime(entity.getCrawlTime());

        // 提取任务ID
        if (entity.getTask() != null) {
            dto.setTaskId(entity.getTask().getId());
        }

        return dto;
    }

    // 从DTO转换为实体类（包含任务关联）
    public AsinPriceRank toEntity(CrawlerTask task) {
        AsinPriceRank entity = new AsinPriceRank();
        entity.setId(this.getId());
        entity.setAsin(this.getAsin());
        entity.setBrand(this.getBrand());
        entity.setOriginalPrice(this.getOriginalPrice());
        entity.setLdDiscount(this.getLdDiscount());
        entity.setBdDiscount(this.getBdDiscount());
        entity.setMemberPrice(this.getMemberPrice());
        entity.setMemberFinalPrice(this.getMemberFinalPrice());
        entity.setNonMemberFinalPrice(this.getNonMemberFinalPrice());
        entity.setCoupon(this.getCoupon());
        entity.setDirectDiscount(this.getDirectDiscount());
        entity.setMainCategory(this.getMainCategory());
        entity.setMainCategoryRank(this.getMainCategoryRank());
        entity.setSubCategory(this.getSubCategory());
        entity.setSubCategoryRank(this.getSubCategoryRank());
        entity.setCrawlTime(this.getCrawlTime());

        // 设置任务关联
        entity.setTask(task);

        return entity;
    }
}