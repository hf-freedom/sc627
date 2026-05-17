package com.coupon.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevalidationResult {
    private String templateId;
    private String templateName;
    private int expiredCount;
    private int levelMismatchCount;
    private int maxClaimExceedCount;
    private int totalAffected;
    private LocalDateTime revalidationTime;
}
