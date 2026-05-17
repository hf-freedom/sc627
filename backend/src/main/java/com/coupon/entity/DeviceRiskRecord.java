package com.coupon.entity;

import com.coupon.enums.RiskLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceRiskRecord {
    private String recordId;
    private String deviceId;
    private String userId;
    private RiskLevel riskLevel;
    private Integer claimCountInWindow;
    private LocalDateTime windowStartTime;
    private LocalDateTime createTime;
    private String remark;
}
