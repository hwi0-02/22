package com.example.backend.payment.repository;

import com.example.backend.payment.domain.Payment;
import com.example.backend.payment.domain.Payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // 기존 메소드들
    Page<Payment> findByStatus(Payment.PaymentStatus status, Pageable pageable);
    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.status='COMPLETED' AND p.paidAt BETWEEN :from AND :to")
    BigDecimal sumRevenueBetween(@Param("from") Instant from, @Param("to") Instant to);
    
    // hotel2에서 추가된 admin용 메소드들
    long countByStatus(Payment.PaymentStatus status);
    
       // createdOn 필드 없음 → paidAt을 기준으로 조회
       @Query("SELECT p FROM Payment p WHERE p.paidAt BETWEEN :from AND :to")
       List<Payment> findPaymentsByDateRange(@Param("from") Instant from,
                                                                        @Param("to") Instant to);
    
    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :from AND :to")
       BigDecimal getTotalRevenueByDateRange(@Param("from") Instant from,
                                                                        @Param("to") Instant to);
    
       // 상위 호텔 집계: Reservation.room_id -> Room.hotel_id를 통해 매핑 (네이티브 쿼리)
       @Query(value = "SELECT h.id AS hotelId, h.name AS hotelName, COALESCE(SUM(p.amount),0) AS revenue, COUNT(r.id) AS reservationCount " +
                               "FROM Payment p " +
                               "JOIN Reservation r ON p.reservation_id = r.id " +
                               "JOIN Room rm ON r.room_id = rm.id " +
                               "JOIN Hotel h ON rm.hotel_id = h.id " +
                               "WHERE p.status = 'COMPLETED' AND p.paid_at BETWEEN :from AND :to " +
                               "GROUP BY h.id, h.name " +
                               "ORDER BY revenue DESC",
                 nativeQuery = true)
       List<Object[]> getTopHotelsByRevenue(@Param("from") LocalDateTime from,
                                                                       @Param("to") LocalDateTime to);
    
       @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();
    
    @Query("SELECT FUNCTION('DATE', p.paidAt), COALESCE(SUM(p.amount),0) " +
           "FROM Payment p " +
           "WHERE p.status = 'COMPLETED' AND p.paidAt BETWEEN :from AND :to " +
           "GROUP BY FUNCTION('DATE', p.paidAt) " +
           "ORDER BY FUNCTION('DATE', p.paidAt)")
       List<Object[]> getDailyRevenueByDateRange(@Param("from") Instant from,
                                                                              @Param("to") Instant to);
    // 간소화 모델에서는 위 조인 기반 검색을 지원하지 않음 → 필요 시 별도 View/DTO 조합으로 구현
}
