package com.example.backend.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelAdminDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String phone;
    private String email;
    private String status; // PENDING, APPROVED, REJECTED, SUSPENDED
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Business 정보
    private Long businessId;
    private String businessName;
    private String businessEmail;
    
    // 통계 정보
    private Integer roomCount;
    private Integer reservationCount;
    private Double averageRating;
    private Double totalRevenue;
    
    // 객실 정보
    private List<RoomSummaryDto> rooms;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomSummaryDto {
        private Long id;
        private String roomType;
        private String status;
        private Integer capacity;
        private Double basePrice;
    }
}