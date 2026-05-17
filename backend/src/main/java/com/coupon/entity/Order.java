package com.coupon.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private String orderId;
    private String userId;
    private String userCouponId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private List<String> productCategories;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime cancelTime;
    private Boolean couponReturned;
    private Boolean needAudit;
    private String auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
}
