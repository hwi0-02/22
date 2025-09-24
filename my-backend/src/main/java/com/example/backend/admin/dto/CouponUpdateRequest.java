package com.example.backend.admin.dto;

import com.example.backend.admin.domain.Coupon;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CouponUpdateRequest {
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

    public void updateEntity(Coupon coupon) {
        if (code != null) coupon.setCode(code);
        if (name != null) coupon.setName(name);
        if (description != null) coupon.setDescription(description);
        if (discountType != null) coupon.setDiscountType(discountType);
        if (discountValue != null) coupon.setDiscountValue(discountValue);
        if (minOrderAmount != null) coupon.setMinOrderAmount(minOrderAmount);
        if (maxDiscountAmount != null) coupon.setMaxDiscountAmount(maxDiscountAmount);
        if (usageLimit != null) coupon.setUsageLimit(usageLimit);
        if (validFrom != null) coupon.setValidFrom(validFrom);
        if (validUntil != null) coupon.setValidUntil(validUntil);
        if (status != null) coupon.setStatus(status);
    }
}