package com.example.backend.admin.controller;

import com.example.backend.admin.security.AdminGuard;
import com.example.backend.admin.dto.PaymentAdminDto;
import com.example.backend.payment.domain.Payment;
import com.example.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/payments")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminPaymentController {
    private final AdminGuard adminGuard;
    private final PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<Page<PaymentAdminDto>> list(Authentication authentication,
                                                      @RequestParam(required = false) Payment.PaymentStatus status,
                                                      @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        Page<Payment> page = (status != null)
                ? paymentRepository.findByStatus(status, pageable)
                : paymentRepository.findAll(pageable);
        Page<PaymentAdminDto> dtoPage = new PageImpl<>(
                page.getContent().stream().map(PaymentAdminDto::from).toList(),
                pageable,
                page.getTotalElements()
        );
        return ResponseEntity.ok(dtoPage);
    }

        @PostMapping("/{id}/refund")
        public ResponseEntity<?> refund(Authentication authentication, @PathVariable Long id) {
                adminGuard.requireAdmin(authentication);
                return paymentRepository.findById(id)
                                .map(p -> {
                                        if (p.getStatus() != Payment.PaymentStatus.COMPLETED) {
                                                return ResponseEntity.badRequest().body("COMPLETED 상태만 환불할 수 있습니다.");
                                        }
                                        p.setStatus(Payment.PaymentStatus.REFUNDED);
                                        p.setRefundedAt(java.time.Instant.now());
                                        paymentRepository.save(p);
                                        return ResponseEntity.ok(PaymentAdminDto.from(p));
                                })
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }
}
