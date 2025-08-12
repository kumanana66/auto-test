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
@Table(name = "asin_price_rank")
public class AsinPriceRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String asin;
    private String brand; // 新增品牌字段

    private Double originalPrice;
    private Double ldDiscount;  // Lightning Deal 折扣
    private Double bdDiscount;  // Best Deal 折扣
    private Double memberPrice; // 会员价
    private Double memberFinalPrice; // 会员最终价
    private Double nonMemberFinalPrice; // 非会员最终价
    private Double coupon;      // 优惠券金额
    private Double directDiscount; // 直降金额

    private String mainCategory;
    private Integer mainCategoryRank;
    private String subCategory;
    private Integer subCategoryRank;

    private LocalDateTime crawlTime;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private CrawlerTask task;
}