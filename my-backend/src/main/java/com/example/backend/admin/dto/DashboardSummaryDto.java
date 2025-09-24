package com.example.backend.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class DashboardSummaryDto {
    
    // 핵심 지표 요약
    private Long totalUsers;
    private Long totalBusinesses;
    private Long totalReservations;
    private BigDecimal totalRevenue;
    private Long totalReviews;
    private Long totalCoupons;
    
    // 최근 30일간 일별 매출액
    private List<DailyRevenue> dailyRevenues;
    
    // 최근 12개월간 월별 신규 가입자 수
    private List<MonthlySignups> monthlySignups;
    
    // 최근 30일간 예약 수가 가장 많은 상위 5개 호텔
    private List<TopHotel> topHotels;
    
    @Getter
    @Setter
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
    }
    
    @Getter
    @Setter
    @Builder
    public static class MonthlySignups {
        private String month; // "2024-01" 형태
        private Long userCount;
        private Long businessCount;
    }
    
    @Getter
    @Setter
    @Builder
    public static class TopHotel {
        private Long hotelId;
        private String hotelName;
        private String businessName;
        private Long reservationCount;
        private BigDecimal revenue;
        private Double averageRating;
    }
}