package com.example.backend.admin.repository;

import com.example.backend.admin.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT c FROM Coupon c WHERE (:status IS NULL OR c.status = :status) AND (:discountType IS NULL OR c.discountType = :discountType) AND (:code IS NULL OR LOWER(c.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Coupon> findCouponsWithFilters(@Param("status") Coupon.CouponStatus status,
                                        @Param("discountType") Coupon.DiscountType discountType,
                                        @Param("code") String code,
                                        @Param("name") String name,
                                        Pageable pageable);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    long countByStatus(Coupon.CouponStatus status);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.validUntil < :now AND c.status = 'ACTIVE'")
    long countExpiredCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.usageLimit IS NOT NULL AND c.usedCount >= c.usageLimit")
    long countUsedUpCoupons();

    @Query("SELECT c FROM Coupon c WHERE c.validUntil < :now AND c.status = 'ACTIVE'")
    Page<Coupon> findExpiredActiveCoupons(@Param("now") LocalDateTime now, Pageable pageable);
}
