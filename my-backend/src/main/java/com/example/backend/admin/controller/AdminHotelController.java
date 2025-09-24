package com.example.backend.admin.controller;

import com.example.backend.dto.hotel.HotelAdminDto;
import com.example.backend.dto.hotel.HotelStatusUpdateRequest;
import com.example.backend.fe_hotel_detail.domain.Hotel;
import com.example.backend.fe_hotel_detail.repository.HotelRepository;
import com.example.backend.hotel_reservation.repository.RoomRepository;
import com.example.backend.hotel_reservation.repository.ReservationRepository;
import com.example.backend.review.repository.ReviewRepository;
import com.example.backend.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.example.backend.admin.security.AdminGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminHotelController {

    private final HotelRepository hotelRepository;
    private final AdminGuard adminGuard;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    @GetMapping
    public ResponseEntity<Page<HotelAdminDto>> list(Authentication authentication, @PageableDefault(size = 20) Pageable pageable) {
        adminGuard.requireAdmin(authentication);
        Page<Hotel> hotels = hotelRepository.findAll(pageable);
    Page<HotelAdminDto> page = hotels.map(h -> HotelAdminDto.builder()
                .id(h.getId())
                .name(h.getName())
                .address(h.getAddress())
                .description(h.getDescription())
                .city(h.getCountry())
        .status(h.getStatus() != null ? h.getStatus().name() : "PENDING")
        .rejectionReason(h.getRejectionReason())
                .build());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        return hotelRepository.findById(id)
        .<ResponseEntity<?>>map(h -> ResponseEntity.ok(HotelAdminDto.builder()
                        .id(h.getId())
                        .name(h.getName())
                        .address(h.getAddress())
                        .description(h.getDescription())
                        .city(h.getCountry())
            .status(h.getStatus() != null ? h.getStatus().name() : "PENDING")
            .rejectionReason(h.getRejectionReason())
                        .build()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(Authentication authentication, @PathVariable Long id, @RequestBody HotelStatusUpdateRequest req) {
        adminGuard.requireAdmin(authentication);
    return hotelRepository.findById(id)
        .<ResponseEntity<?>>map(h -> {
            if (req.getStatus() != null) {
            h.setStatus(Hotel.HotelStatus.valueOf(req.getStatus()));
            }
            h.setRejectionReason(req.getReason());
            hotelRepository.save(h);
            return ResponseEntity.ok(Map.of(
                "id", h.getId(),
                "status", h.getStatus().name(),
                "rejectionReason", h.getRejectionReason()
            ));
        })
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<?> stats(Authentication authentication, @PathVariable Long id) {
        adminGuard.requireAdmin(authentication);
        if (!hotelRepository.existsById(id)) return ResponseEntity.notFound().build();
        long roomCount = roomRepository.countByHotelId(id);
        long reservationCount = reservationRepository.count(); // no hotelId on reservation, using total count
        Double avgRating = reviewRepository.findAverageRating(id);
        java.math.BigDecimal totalRevenue = java.math.BigDecimal.ZERO; // per hotel revenue not tracked yet
        return ResponseEntity.ok(java.util.Map.of(
                "roomCount", roomCount,
                "reservationCount", reservationCount,
                "averageRating", avgRating != null ? avgRating : 0.0,
                "totalRevenue", totalRevenue
        ));
    }
}
