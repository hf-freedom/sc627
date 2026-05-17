package com.coupon.service;

import com.coupon.entity.User;
import com.coupon.enums.RiskLevel;
import com.coupon.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    public User createUser(String username, Integer level) {
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setLevel(level);
        DataStore.USERS.put(user.getUserId(), user);
        return user;
    }

    public User getUserById(String userId) {
        return DataStore.USERS.get(userId);
    }

    public List<User> getAllUsers() {
        return DataStore.USERS.values().stream().collect(Collectors.toList());
    }

    public void updateUserRiskLevel(String userId, RiskLevel riskLevel) {
        User user = DataStore.USERS.get(userId);
        if (user != null) {
            user.setRiskLevel(riskLevel);
            if (riskLevel == RiskLevel.HIGH_RISK) {
                user.setNeedAudit(true);
            }
        }
    }

    public void addDeviceId(String userId, String deviceId) {
        User user = DataStore.USERS.get(userId);
        if (user != null && !user.getDeviceIds().contains(deviceId)) {
            user.getDeviceIds().add(deviceId);
        }
    }

    public List<User> getUsersNeedAudit() {
        return DataStore.USERS.values().stream()
                .filter(User::getNeedAudit)
                .collect(Collectors.toList());
    }

    public void approveUser(String userId) {
        User user = DataStore.USERS.get(userId);
        if (user != null) {
            user.setNeedAudit(false);
            user.setRiskLevel(RiskLevel.NORMAL);
        }
    }
}
