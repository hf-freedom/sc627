package com.coupon.entity;

import com.coupon.enums.CouponType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CouponTemplate {
    private String templateId;
    private String name;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer stock;
    private Integer claimedCount;
    private Integer userLevelRequired;
    private Integer userMaxClaim;
    private List<String> productCategories;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean active;
}
