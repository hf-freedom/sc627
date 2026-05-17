package com.coupon.store;

import com.coupon.entity.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    public static final Map<String, CouponTemplate> COUPON_TEMPLATES = new ConcurrentHashMap<>();
    public static final Map<String, UserCoupon> USER_COUPONS = new ConcurrentHashMap<>();
    public static final Map<String, User> USERS = new ConcurrentHashMap<>();
    public static final Map<String, Order> ORDERS = new ConcurrentHashMap<>();
    public static final Map<String, DeviceRiskRecord> DEVICE_RISK_RECORDS = new ConcurrentHashMap<>();
}
