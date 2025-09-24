package com.example.backend.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelStatusUpdateRequest {
    private String status; // APPROVED, REJECTED, SUSPENDED
    private String reason; // 승인/거부/정지 사유
}