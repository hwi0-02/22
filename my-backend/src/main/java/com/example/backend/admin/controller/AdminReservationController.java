package com.example.backend.admin.controller;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.admin.dto.ReservationAdminDto;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.hotel_reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.backend.admin.security.AdminGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminReservationController {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final AdminGuard adminGuard;

    @GetMapping
    public ResponseEntity<Page<ReservationAdminDto>> list(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        List<Reservation> all = reservationRepository.findAll();
        List<ReservationAdminDto> mapped = all.stream().map(ReservationAdminDto::from).collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(mapped, pageable, mapped.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
    return reservationRepository.findById(id)
        .<ResponseEntity<?>>map(r -> ResponseEntity.ok(ReservationAdminDto.from(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/count/by-hotel/{hotelId}")
    public ResponseEntity<Long> countByHotel(Authentication authentication, @PathVariable Long hotelId) {
        adminGuard.requireAdmin(authentication);
        long count = reservationService.countByHotelId(hotelId);
        return ResponseEntity.ok(count);
    }
}
