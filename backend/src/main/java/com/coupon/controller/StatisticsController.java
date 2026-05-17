package com.coupon.controller;

import com.coupon.common.Result;
import com.coupon.entity.CouponStatistics;
import com.coupon.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/coupons")
    public Result<List<CouponStatistics>> getCouponStatistics() {
        return Result.success(statisticsService.getCouponStatistics());
    }
}
