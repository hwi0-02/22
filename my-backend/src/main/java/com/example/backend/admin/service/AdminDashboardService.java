package com.example.backend.admin.service;

import com.example.backend.admin.dto.DashboardSummaryDto;
import com.example.backend.admin.repository.BusinessRepository;
import com.example.backend.admin.repository.CouponRepository;
import com.example.backend.authlogin.repository.UserRepository;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.payment.repository.PaymentRepository;
import com.example.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {
    
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final HotelRepository hotelRepository;
    
    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        log.info("[AdminDashboardService] 대시보드 요약 데이터 조회 시작");

        try {
            // 핵심 지표 수집
            Long totalUsers = userRepository.count();
            Long totalBusinesses = businessRepository.count();
            Long totalReservations = reservationRepository.count();
            BigDecimal totalRevenue = paymentRepository.getTotalRevenue();
            Long totalReviews = reviewRepository.count();
            Long totalCoupons = couponRepository.count();

            // 최근 30일간 일별 매출 데이터
            java.time.Instant nowInstant = java.time.Instant.now();
            java.time.Instant thirtyDaysAgoInstant = nowInstant.minus(java.time.Duration.ofDays(30));
            List<Object[]> dailyRevenueData = paymentRepository.getDailyRevenueByDateRange(thirtyDaysAgoInstant, nowInstant);
            List<DashboardSummaryDto.DailyRevenue> dailyRevenues = processDailyRevenueData(dailyRevenueData);

            // 최근 12개월간 월별 신규 가입자 데이터
            LocalDateTime nowLdt = LocalDateTime.now();
            LocalDateTime twelveMonthsAgo = nowLdt.minusMonths(12);
            List<Object[]> userSignupData = userRepository.getMonthlySignupsByDateRange(twelveMonthsAgo, nowLdt);
            List<Object[]> businessSignupData = businessRepository.getMonthlySignupsByDateRange(twelveMonthsAgo, nowLdt);
            List<DashboardSummaryDto.MonthlySignups> monthlySignups = processMonthlySignupData(userSignupData, businessSignupData);

            // 최근 30일간 예약 수가 많은 상위 5개 호텔
            // top hotels native query expects LocalDateTime; convert Instants
            java.time.LocalDateTime ldtFrom = java.time.LocalDateTime.ofInstant(thirtyDaysAgoInstant, java.time.ZoneId.systemDefault());
            java.time.LocalDateTime ldtTo = java.time.LocalDateTime.ofInstant(nowInstant, java.time.ZoneId.systemDefault());
            List<Object[]> topHotelData = paymentRepository.getTopHotelsByRevenue(ldtFrom, ldtTo);
            List<DashboardSummaryDto.TopHotel> topHotels = processTopHotelData(topHotelData);

            DashboardSummaryDto result = DashboardSummaryDto.builder()
                    .totalUsers(totalUsers)
                    .totalBusinesses(totalBusinesses)
                    .totalReservations(totalReservations)
                    .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                    .totalReviews(totalReviews)
                    .totalCoupons(totalCoupons)
                    .dailyRevenues(dailyRevenues)
                    .monthlySignups(monthlySignups)
                    .topHotels(topHotels)
                    .build();

            log.info("[AdminDashboardService] 대시보드 요약 데이터 조회 완료 - 사용자: {}, 사업체: {}, 예약: {}", 
                    totalUsers, totalBusinesses, totalReservations);
            
            return result;

        } catch (Exception e) {
            log.error("[AdminDashboardService] 대시보드 요약 데이터 조회 중 오류 발생", e);
            throw new RuntimeException("대시보드 데이터 조회에 실패했습니다.", e);
        }
    }

    private List<DashboardSummaryDto.DailyRevenue> processDailyRevenueData(List<Object[]> rawData) {
        List<DashboardSummaryDto.DailyRevenue> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Object d = row[0];
            LocalDate date;
            if (d instanceof LocalDate ld) {
                date = ld;
            } else if (d instanceof java.sql.Date sd) {
                date = sd.toLocalDate();
            } else if (d instanceof java.time.Instant inst) {
                date = inst.atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else if (d instanceof java.util.Date ud) {
                date = ud.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            } else {
                // Fallback: skip or use today to avoid NPEs
                continue;
            }
            BigDecimal revenue = (BigDecimal) row[1];
            result.add(DashboardSummaryDto.DailyRevenue.builder()
                    .date(date)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .build());
        }
        return result;
    }

    private List<DashboardSummaryDto.MonthlySignups> processMonthlySignupData(
            List<Object[]> userSignupData, List<Object[]> businessSignupData) {
        List<DashboardSummaryDto.MonthlySignups> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        java.util.Map<String, long[]> map = new java.util.LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            String month = LocalDate.now().minusMonths(11 - i).format(formatter);
            map.put(month, new long[]{0L, 0L}); // [userCount, businessCount]
        }

        if (userSignupData != null) {
            for (Object[] row : userSignupData) {
                String month = String.valueOf(row[0]);
                Number cnt = (Number) row[1];
                if (map.containsKey(month)) {
                    map.get(month)[0] = cnt != null ? cnt.longValue() : 0L;
                }
            }
        }

        if (businessSignupData != null) {
            for (Object[] row : businessSignupData) {
                String month = String.valueOf(row[0]);
                Number cnt = (Number) row[1];
                if (map.containsKey(month)) {
                    map.get(month)[1] = cnt != null ? cnt.longValue() : 0L;
                }
            }
        }

        for (var e : map.entrySet()) {
            result.add(DashboardSummaryDto.MonthlySignups.builder()
                    .month(e.getKey())
                    .userCount(e.getValue()[0])
                    .businessCount(e.getValue()[1])
                    .build());
        }
        return result;
    }

    private List<DashboardSummaryDto.TopHotel> processTopHotelData(List<Object[]> rawData) {
        List<DashboardSummaryDto.TopHotel> result = new ArrayList<>();
        for (Object[] row : rawData) {
            if (result.size() >= 5) break; // 상위 5개만
            Long hotelId = ((Number) row[0]).longValue();
            String hotelName = (String) row[1];
            BigDecimal revenue = (BigDecimal) row[2];
            Long reservationCount = ((Number) row[3]).longValue();

            String businessName = "";
            try {
                var hotelOpt = hotelRepository.findById(hotelId);
                if (hotelOpt.isPresent()) {
                    // 현재 Hotel에 business 매핑이 없으므로 비워둠. 확장 시 여기에 주입.
                    businessName = "";
                }
            } catch (Exception ignored) {}

            Double avgRating = 0.0;
            try {
                Double ar = reviewRepository.findAverageRating(hotelId);
                if (ar != null) avgRating = ar;
            } catch (Exception ignored) {}

            result.add(DashboardSummaryDto.TopHotel.builder()
                    .hotelId(hotelId)
                    .hotelName(hotelName)
                    .businessName(businessName)
                    .reservationCount(reservationCount)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .averageRating(avgRating)
                    .build());
        }
        return result;
    }
}