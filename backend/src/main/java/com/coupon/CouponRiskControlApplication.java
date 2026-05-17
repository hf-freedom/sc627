package com.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableScheduling
@CrossOrigin(origins = "*")
public class CouponRiskControlApplication {
    public static void main(String[] args) {
        SpringApplication.run(CouponRiskControlApplication.class, args);
    }
}
