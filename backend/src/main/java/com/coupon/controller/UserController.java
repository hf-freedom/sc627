package com.coupon.controller;

import com.coupon.common.Result;
import com.coupon.entity.User;
import com.coupon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public Result<User> createUser(@RequestBody Map<String, Object> params) {
        String username = (String) params.get("username");
        Integer level = params.get("level") != null ? ((Number) params.get("level")).intValue() : 1;
        return Result.success(userService.createUser(username, level));
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public Result<User> getUserById(@PathVariable String userId) {
        return Result.success(userService.getUserById(userId));
    }

    @GetMapping("/audit")
    public Result<List<User>> getUsersNeedAudit() {
        return Result.success(userService.getUsersNeedAudit());
    }

    @PostMapping("/approve/{userId}")
    public Result<Void> approveUser(@PathVariable String userId) {
        userService.approveUser(userId);
        return Result.success();
    }
}
