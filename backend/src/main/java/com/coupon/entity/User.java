package com.coupon.entity;

import com.coupon.enums.RiskLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class User {
    private String userId;
    private String username;
    private Integer level;
    private RiskLevel riskLevel;
    private List<String> deviceIds;
    private LocalDateTime createTime;
    private Boolean needAudit;

    public User() {
        this.deviceIds = new ArrayList<>();
        this.riskLevel = RiskLevel.NORMAL;
        this.needAudit = false;
        this.createTime = LocalDateTime.now();
    }
}
