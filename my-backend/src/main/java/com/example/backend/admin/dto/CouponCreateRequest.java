package com.example.backend.admin.dto;

import com.example.backend.admin.domain.Coupon;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponCreateRequest {
    private String code;
    private String name;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Coupon.CouponStatus status;

    public Coupon toEntity() {
        return Coupon.builder()
                .code(code)
                .name(name)
                .description(description)
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount)
                .maxDiscountAmount(maxDiscountAmount)
                .usageLimit(usageLimit)
                .usedCount(0) // 초기값
                .validFrom(validFrom)
                .validUntil(validUntil)
                .status(status != null ? status : Coupon.CouponStatus.ACTIVE)
                .build();
    }
}