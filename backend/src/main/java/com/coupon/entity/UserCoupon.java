package com.coupon.entity;

import com.coupon.enums.CouponStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCoupon {
    private String userCouponId;
    private String templateId;
    private String userId;
    private String deviceId;
    private CouponStatus status;
    private String orderId;
    private LocalDateTime claimTime;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;
    private LocalDateTime returnTime;
}
