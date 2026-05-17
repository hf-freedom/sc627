package com.coupon.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponStatistics {
    private String templateId;
    private String templateName;
    private Integer totalIssued;
    private Integer totalClaimed;
    private Integer totalUsed;
    private Integer totalExpired;
    private Integer totalReturned;
    private Integer riskBlockedCount;
    private BigDecimal totalCost;
    private Double claimRate;
    private Double usageRate;

    public void calculateRates() {
        if (totalIssued > 0) {
            this.claimRate = (double) totalClaimed / totalIssued * 100;
            this.usageRate = (double) totalUsed / totalClaimed * 100;
        }
    }
}
