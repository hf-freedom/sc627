package com.coupon.service;

import com.coupon.entity.CouponStatistics;
import com.coupon.entity.CouponTemplate;
import com.coupon.entity.UserCoupon;
import com.coupon.enums.CouponStatus;
import com.coupon.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatisticsService {

    @Autowired
    private RiskControlService riskControlService;

    public List<CouponStatistics> getCouponStatistics() {
        List<CouponStatistics> statisticsList = new ArrayList<>();

        for (CouponTemplate template : DataStore.COUPON_TEMPLATES.values()) {
            CouponStatistics stats = new CouponStatistics();
            stats.setTemplateId(template.getTemplateId());
            stats.setTemplateName(template.getName());
            stats.setTotalIssued(template.getStock());
            stats.setTotalClaimed(template.getClaimedCount());

            int usedCount = 0;
            int expiredCount = 0;
            int returnedCount = 0;
            BigDecimal totalCost = BigDecimal.ZERO;

            for (UserCoupon uc : DataStore.USER_COUPONS.values()) {
                if (template.getTemplateId().equals(uc.getTemplateId())) {
                    if (uc.getStatus() == CouponStatus.USED) {
                        usedCount++;
                        if (template.getType() != null) {
                            totalCost = totalCost.add(template.getValue());
                        }
                    } else if (uc.getStatus() == CouponStatus.EXPIRED) {
                        expiredCount++;
                    } else if (uc.getStatus() == CouponStatus.RETURNED) {
                        returnedCount++;
                    }
                }
            }

            stats.setTotalUsed(usedCount);
            stats.setTotalExpired(expiredCount);
            stats.setTotalReturned(returnedCount);
            stats.setRiskBlockedCount(riskControlService.getRiskBlockedCount());
            stats.setTotalCost(totalCost);
            stats.calculateRates();

            statisticsList.add(stats);
        }

        return statisticsList;
    }
}
