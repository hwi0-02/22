package com.example.backend.admin.dto;

import com.example.backend.review.domain.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewAdminDto {
    // 리뷰 정보
    private Long reviewId;
    private Integer rating;
    private String content;
    private Boolean isHidden;
    private Boolean isReported;
    private LocalDateTime createdAt;
    private LocalDateTime wroteOn;
    private String image;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    
    // 호텔 정보
    private Long hotelId;
    private String hotelName;
    
    // 예약 정보
    private Long reservationId;

    public static ReviewAdminDto from(Review review) {
        return ReviewAdminDto.builder()
                // 리뷰 정보
                .reviewId(review.getId())
                .rating(review.getRating())
                .content(review.getContent())
                .isHidden(review.isHidden())
                .isReported(review.isReported())
                .createdAt(review.getCreatedAt())
                .wroteOn(review.getWroteOn())
                .image(review.getImage())
                
                // 사용자 정보
                .userId(review.getUserId())
                .userName(null)
                .userEmail(null)
                
                // 호텔 정보
                .hotelId(review.getHotelId())
                .hotelName(null)
                
                // 예약 정보
                .reservationId(review.getReservationId())
                
                .build();
    }
}