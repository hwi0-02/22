package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Coupon;
import com.example.backend.admin.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.backend.admin.security.AdminGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminCouponController {

    private final CouponRepository couponRepository;
    private final AdminGuard adminGuard;

    @GetMapping
    public ResponseEntity<Page<Coupon>> list(Authentication authentication,
            @RequestParam(required = false) Coupon.CouponStatus status,
            @RequestParam(required = false) Coupon.DiscountType discountType,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        return ResponseEntity.ok(couponRepository.findCouponsWithFilters(status, discountType, code, name, pageable));
    }

    @PostMapping
    public ResponseEntity<Coupon> create(Authentication authentication, @RequestBody Coupon coupon) {
        adminGuard.requireAdmin(authentication);
        if (coupon.getUsedCount() == null) coupon.setUsedCount(0);
        if (coupon.getStatus() == null) coupon.setStatus(Coupon.CouponStatus.ACTIVE);
        return ResponseEntity.ok(couponRepository.save(coupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication authentication, @PathVariable Long id, @RequestBody Coupon data) {
        adminGuard.requireAdmin(authentication);
        return couponRepository.findById(id)
                .<ResponseEntity<?>>map(c -> {
                    c.setName(data.getName());
                    c.setDescription(data.getDescription());
                    c.setDiscountType(data.getDiscountType());
                    c.setDiscountValue(data.getDiscountValue());
                    c.setMinOrderAmount(data.getMinOrderAmount());
                    c.setMaxDiscountAmount(data.getMaxDiscountAmount());
                    c.setUsageLimit(data.getUsageLimit());
                    c.setValidFrom(data.getValidFrom());
                    c.setValidUntil(data.getValidUntil());
                    return ResponseEntity.ok(couponRepository.save(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminGuard.requireAdmin(authentication);
        String status = req != null ? String.valueOf(req.get("status")) : null;
        return couponRepository.findById(id)
                .<ResponseEntity<?>>map(c -> {
                    if (status != null) c.setStatus(Coupon.CouponStatus.valueOf(status));
                    return ResponseEntity.ok(couponRepository.save(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        if (couponRepository.existsById(id)) {
            couponRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "deleted"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(Authentication authentication) {
        adminGuard.requireAdmin(authentication);
        long active = couponRepository.countByStatus(Coupon.CouponStatus.ACTIVE);
        long inactive = couponRepository.countByStatus(Coupon.CouponStatus.INACTIVE);
        long expiredActive = couponRepository.countExpiredCoupons(LocalDateTime.now());
        long usedUp = couponRepository.countUsedUpCoupons();
        return ResponseEntity.ok(Map.of(
                "active", active,
                "inactive", inactive,
                "expiredActive", expiredActive,
                "usedUp", usedUp
        ));
    }
}
