package com.example.backend.admin.dto;

import com.example.backend.admin.domain.Coupon;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponAdminDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Coupon.DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Coupon.CouponStatus status;
    private LocalDateTime createdOn;
    
    // 계산된 필드
    private Boolean isValid;
    private Integer remainingUsage;
    private String discountDisplay;

    public static CouponAdminDto from(Coupon coupon) {
        // 남은 사용 횟수 계산
        Integer remainingUsage = null;
        if (coupon.getUsageLimit() != null) {
            remainingUsage = Math.max(0, coupon.getUsageLimit() - coupon.getUsedCount());
        }

        // 할인 표시 텍스트 생성
        String discountDisplay;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discountDisplay = coupon.getDiscountValue() + "%";
        } else {
            discountDisplay = "₩" + coupon.getDiscountValue().intValue();
        }

        return CouponAdminDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .status(coupon.getStatus())
                .createdOn(coupon.getCreatedAt())
                .isValid(coupon.isValid())
                .remainingUsage(remainingUsage)
                .discountDisplay(discountDisplay)
                .build();
    }
}