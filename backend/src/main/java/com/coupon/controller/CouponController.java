package com.coupon.controller;

import com.coupon.common.Result;
import com.coupon.entity.CouponTemplate;
import com.coupon.entity.RevalidationResult;
import com.coupon.entity.UserCoupon;
import com.coupon.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupon")
@CrossOrigin(origins = "*")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping("/template")
    public Result<CouponTemplate> createTemplate(@RequestBody CouponTemplate template) {
        CouponTemplate result = couponService.createTemplate(
                template.getName(),
                template.getType(),
                template.getValue(),
                template.getMinAmount(),
                template.getStock(),
                template.getUserLevelRequired(),
                template.getUserMaxClaim(),
                template.getProductCategories(),
                template.getValidEndTime()
        );
        return Result.success(result);
    }

    @GetMapping("/templates")
    public Result<List<CouponTemplate>> getAllTemplates() {
        return Result.success(couponService.getAllTemplates());
    }

    @GetMapping("/template/{templateId}")
    public Result<CouponTemplate> getTemplateById(@PathVariable String templateId) {
        return Result.success(couponService.getTemplateById(templateId));
    }

    @PutMapping("/template/{templateId}")
    public Result<RevalidationResult> updateTemplateRule(@PathVariable String templateId, @RequestBody CouponTemplate updatedTemplate) {
        couponService.updateTemplateRule(templateId, updatedTemplate);
        RevalidationResult result = couponService.revalidateUnusedCoupons(templateId);
        return Result.success(result);
    }

    @PostMapping("/template/{templateId}/revalidate")
    public Result<RevalidationResult> revalidateCoupons(@PathVariable String templateId) {
        RevalidationResult result = couponService.revalidateUnusedCoupons(templateId);
        return Result.success(result);
    }

    @PostMapping("/claim")
    public Result<UserCoupon> claimCoupon(@RequestBody Map<String, String> params) {
        return couponService.claimCoupon(
                params.get("templateId"),
                params.get("userId"),
                params.get("deviceId")
        );
    }

    @GetMapping("/user/{userId}")
    public Result<List<UserCoupon>> getUserCoupons(@PathVariable String userId) {
        return Result.success(couponService.getUserCoupons(userId));
    }

    @GetMapping("/user/all")
    public Result<List<UserCoupon>> getAllUserCoupons() {
        return Result.success(couponService.getAllUserCoupons());
    }
}
