package com.example.backend.admin.controller;

import com.example.backend.admin.domain.Business;
import com.example.backend.admin.repository.BusinessRepository;
import com.example.backend.admin.security.AdminGuard;
import com.example.backend.admin.service.AdminDashboardService;
import com.example.backend.admin.dto.DashboardSummaryDto;
import com.example.backend.authlogin.repository.LoginRepository;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.hotel_reservation.domain.ReservationStatus;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminGuard adminGuard;
    private final AdminDashboardService adminDashboardService;
    private final LoginRepository loginRepository;
    private final BusinessRepository businessRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<?> overview(Authentication authentication) {
        adminGuard.requireAdmin(authentication);

        long users = loginRepository.count();
        long businessesPending = businessRepository.countByStatus(Business.BusinessStatus.PENDING);
        long hotels = hotelRepository.count();

        LocalDate today = LocalDate.now();
        Instant startOfDay = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

    long reservationsToday = reservationRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        BigDecimal revenueToday = paymentRepository.sumRevenueBetween(startOfDay, endOfDay);

        return ResponseEntity.ok(Map.of(
                "users", users,
                "businessesPending", businessesPending,
                "hotels", hotels,
                "reservationsToday", reservationsToday,
                "revenueToday", revenueToday
        ));
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardSummaryDto> getDetailedStats(Authentication authentication) {
        adminGuard.requireAdmin(authentication);
        DashboardSummaryDto stats = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(stats);
    }
}
