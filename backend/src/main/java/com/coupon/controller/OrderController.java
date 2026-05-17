package com.coupon.controller;

import com.coupon.common.Result;
import com.coupon.entity.Order;
import com.coupon.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        String userCouponId = (String) params.get("userCouponId");
        BigDecimal totalAmount = new BigDecimal(params.get("totalAmount").toString());
        List<String> productCategories = (List<String>) params.get("productCategories");
        return orderService.createOrder(userId, userCouponId, totalAmount, productCategories);
    }

    @PostMapping("/cancel/{orderId}")
    public Result<Void> cancelOrder(@PathVariable String orderId) {
        return orderService.cancelOrder(orderId);
    }

    @GetMapping
    public Result<List<Order>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable String userId) {
        return Result.success(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    public Result<Order> getOrderById(@PathVariable String orderId) {
        return Result.success(orderService.getOrderById(orderId));
    }

    @GetMapping("/audit/pending")
    public Result<List<Order>> getOrdersNeedAudit() {
        return Result.success(orderService.getOrdersNeedAudit());
    }

    @PostMapping("/audit/{orderId}")
    public Result<Void> auditOrder(@PathVariable String orderId, @RequestBody Map<String, String> params) {
        String auditResult = params.get("auditResult");
        String auditRemark = params.get("auditRemark");
        return orderService.auditOrder(orderId, auditResult, auditRemark);
    }
}
