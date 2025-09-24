package com.example.backend.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "Payment")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="reservation_id", nullable=false)
    private Long reservationId;

    @Column(name="amount", nullable=false, precision=12, scale=2)
    private BigDecimal amount;

    @Column(name="base_price", precision=12, scale=2)
    private BigDecimal basePrice;

    @Column(name="total_price", precision=12, scale=2)
    private BigDecimal totalPrice;

    @Column(name="tax", precision=12, scale=2)
    private BigDecimal tax;

    @Column(name="discount", precision=12, scale=2)
    private BigDecimal discount;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(name="method", length=50)
    private String method;

    @Column(name="transaction_id", length=100)
    private String transactionId;

    @Column(name="paid_at")
    private Instant paidAt;

    @Column(name="created_at")
    private Instant createdAt;

    @Column(name="refunded_at")
    private Instant refundedAt;

    @Column(name="receipt_url", length=500)
    private String receiptUrl;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
}
