package com.coupon.service;

import com.coupon.common.Result;
import com.coupon.entity.*;
import com.coupon.enums.CouponStatus;
import com.coupon.enums.CouponType;
import com.coupon.enums.RiskLevel;
import com.coupon.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private UserService userService;

    @Autowired
    private RiskControlService riskControlService;

    public CouponTemplate createTemplate(String name, CouponType type, BigDecimal value,
                                          BigDecimal minAmount, Integer stock, Integer userLevelRequired,
                                          Integer userMaxClaim, List<String> productCategories,
                                          LocalDateTime validEndTime) {
        CouponTemplate template = new CouponTemplate();
        template.setTemplateId(UUID.randomUUID().toString());
        template.setName(name);
        template.setType(type);
        template.setValue(value);
        template.setMinAmount(minAmount);
        template.setStock(stock);
        template.setClaimedCount(0);
        template.setUserLevelRequired(userLevelRequired);
        template.setUserMaxClaim(userMaxClaim);
        template.setProductCategories(productCategories);
        template.setValidStartTime(LocalDateTime.now());
        template.setValidEndTime(validEndTime);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        template.setActive(true);
        DataStore.COUPON_TEMPLATES.put(template.getTemplateId(), template);
        return template;
    }

    public Result<UserCoupon> claimCoupon(String templateId, String userId, String deviceId) {
        CouponTemplate template = DataStore.COUPON_TEMPLATES.get(templateId);
        if (template == null) {
            return Result.error("优惠券不存在");
        }
        if (!template.getActive()) {
            return Result.error("优惠券活动已结束");
        }
        if (template.getStock() - template.getClaimedCount() <= 0) {
            return Result.error("优惠券已抢完");
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (user.getLevel() < template.getUserLevelRequired()) {
            return Result.error("用户等级不足");
        }

        long userClaimCount = DataStore.USER_COUPONS.values().stream()
                .filter(uc -> templateId.equals(uc.getTemplateId()) && userId.equals(uc.getUserId()))
                .count();
        if (userClaimCount >= template.getUserMaxClaim()) {
            return Result.error("超出领取限制");
        }

        RiskLevel riskLevel = riskControlService.checkDeviceRisk(deviceId, userId);
        if (riskLevel != RiskLevel.NORMAL) {
            riskControlService.incrementRiskBlockedCount();
            return Result.error("检测到异常操作，暂时无法领取");
        }

        if (riskControlService.needManualAudit(userId)) {
            return Result.error(403, "账户需要人工审核");
        }

        userService.addDeviceId(userId, deviceId);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserCouponId(UUID.randomUUID().toString());
        userCoupon.setTemplateId(templateId);
        userCoupon.setUserId(userId);
        userCoupon.setDeviceId(deviceId);
        userCoupon.setStatus(CouponStatus.CLAIMED);
        userCoupon.setClaimTime(LocalDateTime.now());
        userCoupon.setExpireTime(template.getValidEndTime());
        DataStore.USER_COUPONS.put(userCoupon.getUserCouponId(), userCoupon);

        template.setClaimedCount(template.getClaimedCount() + 1);
        template.setUpdateTime(LocalDateTime.now());

        return Result.success(userCoupon);
    }

    public Result<BigDecimal> useCoupon(String userCouponId, String userId, BigDecimal orderAmount, List<String> productCategories) {
        UserCoupon userCoupon = DataStore.USER_COUPONS.get(userCouponId);
        if (userCoupon == null) {
            return Result.error("优惠券不存在");
        }
        if (!userCoupon.getUserId().equals(userId)) {
            return Result.error("优惠券不属于当前用户");
        }
        if (userCoupon.getStatus() != CouponStatus.CLAIMED) {
            return Result.error("优惠券状态异常");
        }
        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            return Result.error("优惠券已过期");
        }

        CouponTemplate template = DataStore.COUPON_TEMPLATES.get(userCoupon.getTemplateId());
        if (template == null) {
            return Result.error("优惠券模板不存在");
        }

        if (orderAmount.compareTo(template.getMinAmount()) < 0) {
            return Result.error("订单金额不足");
        }

        boolean categoryValid = false;
        for (String pc : productCategories) {
            if (template.getProductCategories().contains(pc)) {
                categoryValid = true;
                break;
            }
        }
        if (!categoryValid) {
            return Result.error("商品不支持使用此优惠券");
        }

        if (riskControlService.needManualAudit(userId)) {
            return Result.error(403, "账户需要人工审核后才能使用优惠券");
        }

        BigDecimal discount;
        if (template.getType() == CouponType.FIXED_AMOUNT) {
            discount = template.getValue();
        } else {
            discount = orderAmount.multiply(template.getValue()).divide(new BigDecimal(100));
        }

        userCoupon.setStatus(CouponStatus.USED);
        userCoupon.setUseTime(LocalDateTime.now());

        return Result.success(discount);
    }

    public Result<Void> returnCoupon(String userCouponId) {
        UserCoupon userCoupon = DataStore.USER_COUPONS.get(userCouponId);
        if (userCoupon == null) {
            return Result.error("优惠券不存在");
        }
        if (userCoupon.getStatus() != CouponStatus.USED) {
            return Result.error("优惠券未使用");
        }

        CouponTemplate template = DataStore.COUPON_TEMPLATES.get(userCoupon.getTemplateId());
        if (template == null || !template.getActive()) {
            return Result.error("活动已结束，无法退回");
        }

        if (LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            return Result.error("优惠券已过期，无法退回");
        }

        userCoupon.setStatus(CouponStatus.RETURNED);
        userCoupon.setReturnTime(LocalDateTime.now());

        return Result.success();
    }

    public void markAsUsed(String userCouponId, String orderId) {
        UserCoupon userCoupon = DataStore.USER_COUPONS.get(userCouponId);
        if (userCoupon != null) {
            userCoupon.setOrderId(orderId);
        }
    }

    public List<CouponTemplate> getAllTemplates() {
        return DataStore.COUPON_TEMPLATES.values().stream().collect(Collectors.toList());
    }

    public List<UserCoupon> getUserCoupons(String userId) {
        return DataStore.USER_COUPONS.values().stream()
                .filter(uc -> userId.equals(uc.getUserId()))
                .collect(Collectors.toList());
    }

    public List<UserCoupon> getAllUserCoupons() {
        return DataStore.USER_COUPONS.values().stream().collect(Collectors.toList());
    }

    public CouponTemplate getTemplateById(String templateId) {
        return DataStore.COUPON_TEMPLATES.get(templateId);
    }

    public void updateTemplateRule(String templateId, CouponTemplate updatedTemplate) {
        CouponTemplate template = DataStore.COUPON_TEMPLATES.get(templateId);
        if (template != null) {
            if (updatedTemplate.getName() != null) {
                template.setName(updatedTemplate.getName());
            }
            if (updatedTemplate.getUserLevelRequired() != null) {
                template.setUserLevelRequired(updatedTemplate.getUserLevelRequired());
            }
            if (updatedTemplate.getUserMaxClaim() != null) {
                template.setUserMaxClaim(updatedTemplate.getUserMaxClaim());
            }
            if (updatedTemplate.getMinAmount() != null) {
                template.setMinAmount(updatedTemplate.getMinAmount());
            }
            if (updatedTemplate.getProductCategories() != null) {
                template.setProductCategories(updatedTemplate.getProductCategories());
            }
            if (updatedTemplate.getValidEndTime() != null) {
                template.setValidEndTime(updatedTemplate.getValidEndTime());
            }
            if (updatedTemplate.getActive() != null) {
                template.setActive(updatedTemplate.getActive());
            }
            template.setUpdateTime(LocalDateTime.now());
        }
    }

    public RevalidationResult revalidateUnusedCoupons(String templateId) {
        CouponTemplate template = DataStore.COUPON_TEMPLATES.get(templateId);
        if (template == null) return null;

        RevalidationResult result = new RevalidationResult();
        result.setTemplateId(templateId);
        result.setTemplateName(template.getName());

        int expiredCount = 0;
        int levelMismatchCount = 0;
        int maxClaimExceedCount = 0;

        for (UserCoupon uc : DataStore.USER_COUPONS.values()) {
            if (!templateId.equals(uc.getTemplateId())) continue;
            if (uc.getStatus() != CouponStatus.CLAIMED) continue;

            User user = DataStore.USERS.get(uc.getUserId());

            if (LocalDateTime.now().isAfter(template.getValidEndTime())) {
                uc.setStatus(CouponStatus.EXPIRED);
                expiredCount++;
                continue;
            }

            if (user != null && user.getLevel() < template.getUserLevelRequired()) {
                levelMismatchCount++;
            }

            long userClaimCount = DataStore.USER_COUPONS.values().stream()
                    .filter(c -> templateId.equals(c.getTemplateId()) && uc.getUserId().equals(c.getUserId()))
                    .filter(c -> c.getStatus() == CouponStatus.CLAIMED || c.getStatus() == CouponStatus.USED)
                    .count();
            if (userClaimCount > template.getUserMaxClaim()) {
                maxClaimExceedCount++;
            }
        }

        result.setExpiredCount(expiredCount);
        result.setLevelMismatchCount(levelMismatchCount);
        result.setMaxClaimExceedCount(maxClaimExceedCount);
        result.setTotalAffected(expiredCount + levelMismatchCount + maxClaimExceedCount);
        result.setRevalidationTime(LocalDateTime.now());

        return result;
    }

    public void expireCoupons() {
        LocalDateTime now = LocalDateTime.now();
        DataStore.USER_COUPONS.values().stream()
                .filter(uc -> uc.getStatus() == CouponStatus.CLAIMED)
                .filter(uc -> now.isAfter(uc.getExpireTime()))
                .forEach(uc -> uc.setStatus(CouponStatus.EXPIRED));
    }
}
