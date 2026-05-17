package com.coupon.service;

import com.coupon.entity.DeviceRiskRecord;
import com.coupon.entity.User;
import com.coupon.entity.UserCoupon;
import com.coupon.enums.RiskLevel;
import com.coupon.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RiskControlService {

    @Autowired
    private UserService userService;

    private static final int MAX_CLAIMS_PER_DEVICE = 5;
    private static final int TIME_WINDOW_MINUTES = 30;
    private int riskBlockedCount = 0;

    public RiskLevel checkDeviceRisk(String deviceId, String userId) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(TIME_WINDOW_MINUTES);

        long claimCount = DataStore.USER_COUPONS.values().stream()
                .filter(uc -> deviceId.equals(uc.getDeviceId()))
                .filter(uc -> uc.getClaimTime().isAfter(windowStart))
                .count();

        if (claimCount >= MAX_CLAIMS_PER_DEVICE) {
            createRiskRecord(deviceId, userId, RiskLevel.SUSPECTED, (int) claimCount, windowStart, "设备领取过于频繁");
            userService.updateUserRiskLevel(userId, RiskLevel.SUSPECTED);
            return RiskLevel.SUSPECTED;
        }

        return RiskLevel.NORMAL;
    }

    public boolean needManualAudit(String userId) {
        User user = userService.getUserById(userId);
        return user != null && user.getNeedAudit();
    }

    private void createRiskRecord(String deviceId, String userId, RiskLevel level, int claimCount, LocalDateTime windowStart, String remark) {
        DeviceRiskRecord record = new DeviceRiskRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setDeviceId(deviceId);
        record.setUserId(userId);
        record.setRiskLevel(level);
        record.setClaimCountInWindow(claimCount);
        record.setWindowStartTime(windowStart);
        record.setCreateTime(LocalDateTime.now());
        record.setRemark(remark);
        DataStore.DEVICE_RISK_RECORDS.put(record.getRecordId(), record);
    }

    public void incrementRiskBlockedCount() {
        riskBlockedCount++;
    }

    public int getRiskBlockedCount() {
        return riskBlockedCount;
    }

    public List<DeviceRiskRecord> getAllRiskRecords() {
        return DataStore.DEVICE_RISK_RECORDS.values().stream().collect(Collectors.toList());
    }

    public List<DeviceRiskRecord> getRiskRecordsByDevice(String deviceId) {
        return DataStore.DEVICE_RISK_RECORDS.values().stream()
                .filter(r -> deviceId.equals(r.getDeviceId()))
                .collect(Collectors.toList());
    }
}
