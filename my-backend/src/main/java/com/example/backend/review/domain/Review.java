package com.example.backend.review.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Review")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="hotel_id", nullable=false)
    private Long hotelId;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="reservation_id")
    private Long reservationId;

    @Column(nullable=false)
    private Integer rating; // 1~5

    @Column(columnDefinition="TEXT")
    private String content;

    @Column(name="image", length = 500)
    private String image;

    @Column(name="hidden", nullable=false)
    private boolean hidden;

    @Column(name="reported", nullable=false)
    private boolean reported;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @Column(name="wrote_on")
    private LocalDateTime wroteOn;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (wroteOn == null) wroteOn = now;
    }
}
