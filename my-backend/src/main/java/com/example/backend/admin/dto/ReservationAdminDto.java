package com.example.backend.admin.dto;

import com.example.backend.hotel_reservation.domain.Reservation;
import com.example.backend.hotel_reservation.domain.ReservationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
public class ReservationAdminDto {
    // 예약 정보
    private Long reservationId;
    private ReservationStatus reservationStatus;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime reservationCreatedOn;
    
    // 결제 정보
    private Long paymentId;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal paidAmount;
    private LocalDateTime paymentCreatedOn;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    
    // 호텔/객실 정보
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomName;
    private String roomType;

    public static ReservationAdminDto from(Reservation reservation) {
        LocalDate checkIn = reservation.getStartDate() != null ? reservation.getStartDate().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        LocalDate checkOut = reservation.getEndDate() != null ? reservation.getEndDate().atZone(ZoneId.systemDefault()).toLocalDate() : null;
        Integer guestCount = null;
        try {
            guestCount = (reservation.getNumAdult() != null ? reservation.getNumAdult() : 0)
                    + (reservation.getNumKid() != null ? reservation.getNumKid() : 0);
        } catch (Exception ignored) {}
        return ReservationAdminDto.builder()
                // 예약 정보
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getStatus())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .guestCount(guestCount)
                .totalAmount(null)
                .specialRequests(null)
                .reservationCreatedOn(reservation.getCreatedAt() != null ? LocalDateTime.ofInstant(reservation.getCreatedAt(), ZoneId.systemDefault()) : null)
                
                // 결제 정보
                .paymentId(null)
                .paymentStatus(null)
                .paymentMethod(null)
                .paidAmount(null)
                .paymentCreatedOn(null)
                
                // 사용자 정보
                .userId(reservation.getUserId())
                .userName(null)
                .userEmail(null)
                
                // 호텔/객실 정보
                .hotelId(null)
                .hotelName(null)
                .roomId(reservation.getRoomId())
                .roomName(null)
                .roomType(null)
                
                .build();
    }
}