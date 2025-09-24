package com.example.backend.admin.dto;

import com.example.backend.admin.domain.Business;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BusinessAdminDto {
    private Long id;
    private String businessName;
    private String businessNumber;
    private String address;
    private String phone;
    private Business.BusinessStatus status;
    private LocalDateTime createdOn;
    private LocalDateTime updatedAt;

    // User 정보도 포함
    private Long userId;
    private String userName;
    private String userEmail;

    public static BusinessAdminDto from(Business business) {
        return BusinessAdminDto.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .businessNumber(business.getBusinessNumber())
                .address(business.getAddress())
                .phone(business.getPhone())
                .status(business.getStatus())
                .createdOn(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .userId(business.getUser() != null ? business.getUser().getId() : null)
                .userName(business.getUser() != null ? business.getUser().getName() : null)
                .userEmail(business.getUser() != null ? business.getUser().getEmail() : null)
                .build();
    }
}