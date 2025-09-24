package com.example.backend.admin.dto;

import com.example.backend.payment.domain.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAdminDto {
    private Long id;
    private Long reservationId;
    private BigDecimal amount;
    private BigDecimal basePrice;
    private BigDecimal totalPrice;
    private BigDecimal tax;
    private BigDecimal discount;
    private String method;
    private String status;
    private Instant paidAt;
    private Instant refundedAt;
    private String receiptUrl;

    public static PaymentAdminDto from(Payment p) {
        return PaymentAdminDto.builder()
                .id(p.getId())
                .reservationId(p.getReservationId())
        .amount(p.getAmount())
        .basePrice(p.getBasePrice())
        .totalPrice(p.getTotalPrice())
        .tax(p.getTax())
        .discount(p.getDiscount())
                .method(p.getMethod())
                .status(p.getStatus().name())
                .paidAt(p.getPaidAt())
        .refundedAt(p.getRefundedAt())
        .receiptUrl(p.getReceiptUrl())
                .build();
    }
}