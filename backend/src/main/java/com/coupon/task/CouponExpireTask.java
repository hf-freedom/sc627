package com.coupon.task;

import com.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CouponExpireTask {

    @Autowired
    private CouponService couponService;

    @Scheduled(cron = "0 * * * * ?")
    public void expireCoupons() {
        couponService.expireCoupons();
    }
}
