package com.coupon.controller;

import com.coupon.common.Result;
import com.coupon.entity.DeviceRiskRecord;
import com.coupon.service.RiskControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin(origins = "*")
public class RiskController {

    @Autowired
    private RiskControlService riskControlService;

    @GetMapping("/records")
    public Result<List<DeviceRiskRecord>> getAllRiskRecords() {
        return Result.success(riskControlService.getAllRiskRecords());
    }

    @GetMapping("/records/device/{deviceId}")
    public Result<List<DeviceRiskRecord>> getRiskRecordsByDevice(@PathVariable String deviceId) {
        return Result.success(riskControlService.getRiskRecordsByDevice(deviceId));
    }

    @GetMapping("/blocked-count")
    public Result<Integer> getRiskBlockedCount() {
        return Result.success(riskControlService.getRiskBlockedCount());
    }
}
