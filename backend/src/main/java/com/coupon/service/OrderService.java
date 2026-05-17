package com.coupon.service;

import com.coupon.common.Result;
import com.coupon.entity.Order;
import com.coupon.entity.User;
import com.coupon.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private CouponService couponService;

    public Result<Order> createOrder(String userId, String userCouponId, BigDecimal totalAmount, List<String> productCategories) {
        User user = DataStore.USERS.get(userId);
        boolean userNeedAudit = user != null && Boolean.TRUE.equals(user.getNeedAudit());

        BigDecimal discountAmount = BigDecimal.ZERO;

        if (userCouponId != null && !userCouponId.isEmpty()) {
            Result<BigDecimal> useResult = couponService.useCoupon(userCouponId, userId, totalAmount, productCategories);
            if (useResult.getCode() != 200) {
                return Result.error(useResult.getCode(), useResult.getMessage());
            }
            discountAmount = useResult.getData();
        }

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setUserCouponId(userCouponId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setProductCategories(productCategories);
        order.setStatus("CREATED");
        order.setCreateTime(LocalDateTime.now());
        order.setCouponReturned(false);
        
        if (userNeedAudit && userCouponId != null && !userCouponId.isEmpty()) {
            order.setNeedAudit(true);
            order.setAuditStatus("PENDING");
            order.setAuditRemark("异常用户使用优惠券，需人工审核");
        } else {
            order.setNeedAudit(false);
            order.setAuditStatus("NONE");
        }

        DataStore.ORDERS.put(order.getOrderId(), order);

        if (userCouponId != null && !userCouponId.isEmpty()) {
            couponService.markAsUsed(userCouponId, order.getOrderId());
        }

        return Result.success(order);
    }

    public Result<Void> cancelOrder(String orderId) {
        Order order = DataStore.ORDERS.get(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if ("CANCELLED".equals(order.getStatus())) {
            return Result.error("订单已取消");
        }

        order.setStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());

        if (order.getUserCouponId() != null && !order.getCouponReturned()) {
            Result<Void> returnResult = couponService.returnCoupon(order.getUserCouponId());
            if (returnResult.getCode() == 200) {
                order.setCouponReturned(true);
            }
        }

        return Result.success();
    }

    public Order getOrderById(String orderId) {
        return DataStore.ORDERS.get(orderId);
    }

    public List<Order> getAllOrders() {
        return DataStore.ORDERS.values().stream().collect(Collectors.toList());
    }

    public List<Order> getUserOrders(String userId) {
        return DataStore.ORDERS.values().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersNeedAudit() {
        return DataStore.ORDERS.values().stream()
                .filter(o -> Boolean.TRUE.equals(o.getNeedAudit()) && "PENDING".equals(o.getAuditStatus()))
                .collect(Collectors.toList());
    }

    public Result<Void> auditOrder(String orderId, String auditResult, String auditRemark) {
        Order order = DataStore.ORDERS.get(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"PENDING".equals(order.getAuditStatus())) {
            return Result.error("订单无需审核或已审核");
        }

        order.setAuditStatus(auditResult);
        order.setAuditRemark(auditRemark);
        order.setAuditTime(LocalDateTime.now());

        if ("REJECTED".equals(auditResult) && order.getUserCouponId() != null) {
            couponService.returnCoupon(order.getUserCouponId());
            order.setCouponReturned(true);
        }

        return Result.success();
    }
}
